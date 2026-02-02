package com.ia.vt.transactions_service.handler.vt;

import com.ia.vt.transactions_service.benchmark.BenchmarkProperties;
import com.ia.vt.transactions_service.benchmark.PerformanceLogger;
import com.ia.vt.transactions_service.benchmark.RequestLogEntry;
import com.ia.vt.transactions_service.model.ReportRequest;
import com.ia.vt.transactions_service.model.ReportResponse;
import com.ia.vt.transactions_service.service.ReportService;
import com.ia.vt.transactions_service.service.ReportWorkResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDate;

@Component
public class VirtualThreadReportHandler {
	private final BenchmarkProperties properties;
	private final ReportService reportService;
	private final PerformanceLogger performanceLogger;

	public VirtualThreadReportHandler(BenchmarkProperties properties,
									  ReportService reportService,
									  PerformanceLogger performanceLogger) {
		this.properties = properties;
		this.reportService = reportService;
		this.performanceLogger = performanceLogger;
	}

	public ServerResponse handle(ServerRequest request) {
		long startNanos = System.nanoTime();
		RequestLogEntry logEntry = null;
		try {
			ReportRequest reportRequest = parseRequest(request);
			ReportWorkResult workResult = reportService.generateReport(reportRequest, properties.cpuIterations());

			long dbStart = System.nanoTime();
			sleepDbDelay();
			long dbNanos = System.nanoTime() - dbStart;

			long totalNanos = System.nanoTime() - startNanos;
			String threadName = Thread.currentThread().getName();
			String threadType = Thread.currentThread().isVirtual() ? "VIRTUAL_THREAD" : "PLATFORM_THREAD";

			ReportResponse response = new ReportResponse(
					reportRequest.airline(),
					reportRequest.reportDate(),
					nanosToMs(dbNanos),
					nanosToMs(workResult.cpuTimeNanos()),
					nanosToMs(totalNanos),
					workResult.payloadSize(),
					threadName,
					threadType,
					workResult.payload()
			);

			logEntry = new RequestLogEntry(
					"VIRTUAL_THREAD",
					request.path(),
					reportRequest.airline(),
					reportRequest.reportDate().toString(),
					reportRequest.concurrency(),
					nanosToMs(dbNanos),
					nanosToMs(workResult.cpuTimeNanos()),
					nanosToMs(totalNanos),
					workResult.payloadSize(),
					threadName,
					"200"
			);
			performanceLogger.logRequest(logEntry);
			performanceLogger.recordSuccess(logEntry, startNanos, System.nanoTime(), totalNanos);

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(response);
		} catch (IllegalArgumentException ex) {
			long totalNanos = System.nanoTime() - startNanos;
			logEntry = buildFailureLog(request, logEntry, "400", totalNanos);
			performanceLogger.logRequest(logEntry);
			performanceLogger.recordFailure(logEntry, startNanos, System.nanoTime(), totalNanos);
			return ServerResponse.badRequest().body("Invalid request parameters");
		} catch (Exception ex) {
			long totalNanos = System.nanoTime() - startNanos;
			logEntry = buildFailureLog(request, logEntry, "500", totalNanos);
			performanceLogger.logRequest(logEntry);
			performanceLogger.recordFailure(logEntry, startNanos, System.nanoTime(), totalNanos);
			return ServerResponse.status(500).body("Internal error");
		}
	}

	private ReportRequest parseRequest(ServerRequest request) {
		String airline = request.param("airline").orElse(properties.defaultAirline());
		String dateText = request.param("date").orElse(properties.defaultDate());
		String concurrencyText = request.param("c").orElse(String.valueOf(properties.defaultConcurrency()));
		String payloadText = request.param("payload").orElse(String.valueOf(properties.payloadBytes()));

		LocalDate date = LocalDate.parse(dateText);
		int concurrency = Integer.parseInt(concurrencyText);
		int payload = Integer.parseInt(payloadText);
		if (concurrency <= 0 || payload < 0) {
			throw new IllegalArgumentException("Invalid concurrency or payload");
		}
		return new ReportRequest(airline, date, concurrency, payload);
	}

	private void sleepDbDelay() {
		try {
			Thread.sleep(properties.dbDelayMs());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("DB delay interrupted", ex);
		}
	}

	private long nanosToMs(long nanos) {
		return nanos / 1_000_000L;
	}

	private RequestLogEntry buildFailureLog(ServerRequest request, RequestLogEntry existing, String status, long totalNanos) {
		String endpoint = existing != null ? existing.endpoint() : request.path();
		String airline = existing != null ? existing.airline() : properties.defaultAirline();
		String reportDate = existing != null ? existing.reportDate() : properties.defaultDate();
		Integer concurrency = existing != null ? existing.concurrency() : properties.defaultConcurrency();
		Integer payload = existing != null ? existing.payloadBytes() : properties.payloadBytes();
		String threadName = Thread.currentThread().getName();
		return new RequestLogEntry(
				"VIRTUAL_THREAD",
				endpoint,
				airline,
				reportDate,
				concurrency,
				null,
				null,
				nanosToMs(totalNanos),
				payload,
				threadName,
				status
		);
	}
}
