package com.ia.vt.transactions_service.handler.wf;

import com.ia.vt.transactions_service.benchmark.BenchmarkProperties;
import com.ia.vt.transactions_service.benchmark.PerformanceLogger;
import com.ia.vt.transactions_service.benchmark.RequestLogEntry;
import com.ia.vt.transactions_service.model.ReportRequest;
import com.ia.vt.transactions_service.model.ReportResponse;
import com.ia.vt.transactions_service.service.ReportService;
import com.ia.vt.transactions_service.service.ReportWorkResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;

@Component
public class WebFluxReportHandler {
	private final BenchmarkProperties properties;
	private final ReportService reportService;
	private final PerformanceLogger performanceLogger;

	public WebFluxReportHandler(BenchmarkProperties properties,
								ReportService reportService,
								PerformanceLogger performanceLogger) {
		this.properties = properties;
		this.reportService = reportService;
		this.performanceLogger = performanceLogger;
	}

	public Mono<ServerResponse> handle(ServerRequest request) {
		return Mono.defer(() -> {
			long startNanos = System.nanoTime();
			final ReportRequest reportRequest;
			try {
				reportRequest = parseRequest(request);
			} catch (IllegalArgumentException ex) {
				RequestLogEntry logEntry = buildFailureLog(request, "400", startNanos);
				performanceLogger.logRequest(logEntry);
				performanceLogger.recordFailure(logEntry, startNanos, System.nanoTime(), System.nanoTime() - startNanos);
				return ServerResponse.badRequest().bodyValue("Invalid request parameters");
			}

			return Mono.fromSupplier(() -> reportService.generateReport(reportRequest, properties.cpuIterations()))
					.flatMap(workResult -> Mono.defer(() -> {
						long dbStart = System.nanoTime();
						return Mono.delay(Duration.ofMillis(properties.dbDelayMs()))
								.map(ignored -> new DbResult(workResult, System.nanoTime() - dbStart));
					}))
					.flatMap(result -> {
						long totalNanos = System.nanoTime() - startNanos;
						String threadName = Thread.currentThread().getName();
						String threadType = "EVENT_LOOP";
						ReportResponse response = new ReportResponse(
								reportRequest.airline(),
								reportRequest.reportDate(),
								nanosToMs(result.dbNanos()),
								nanosToMs(result.workResult().cpuTimeNanos()),
								nanosToMs(totalNanos),
								result.workResult().payloadSize(),
								threadName,
								threadType,
								result.workResult().payload()
						);

						RequestLogEntry logEntry = new RequestLogEntry(
								"EVENT_LOOP",
								request.path(),
								reportRequest.airline(),
								reportRequest.reportDate().toString(),
								reportRequest.concurrency(),
								nanosToMs(result.dbNanos()),
								nanosToMs(result.workResult().cpuTimeNanos()),
								nanosToMs(totalNanos),
								result.workResult().payloadSize(),
								threadName,
								"200"
						);
						performanceLogger.logRequest(logEntry);
						performanceLogger.recordSuccess(logEntry, startNanos, System.nanoTime(), totalNanos);

						return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.bodyValue(response);
					})
					.onErrorResume(ex -> {
						RequestLogEntry logEntry = buildFailureLog(request, "500", startNanos);
						performanceLogger.logRequest(logEntry);
						performanceLogger.recordFailure(logEntry, startNanos, System.nanoTime(), System.nanoTime() - startNanos);
						return ServerResponse.status(500).bodyValue("Internal error");
					});
		});
	}

	private ReportRequest parseRequest(ServerRequest request) {
		String airline = request.queryParam("airline").orElse(properties.defaultAirline());
		String dateText = request.queryParam("date").orElse(properties.defaultDate());
		String concurrencyText = request.queryParam("c").orElse(String.valueOf(properties.defaultConcurrency()));
		String payloadText = request.queryParam("payload").orElse(String.valueOf(properties.payloadBytes()));

		LocalDate date = LocalDate.parse(dateText);
		int concurrency = Integer.parseInt(concurrencyText);
		int payload = Integer.parseInt(payloadText);
		if (concurrency <= 0 || payload < 0) {
			throw new IllegalArgumentException("Invalid concurrency or payload");
		}
		return new ReportRequest(airline, date, concurrency, payload);
	}

	private long nanosToMs(long nanos) {
		return nanos / 1_000_000L;
	}

	private RequestLogEntry buildFailureLog(ServerRequest request, String status, long startNanos) {
		long totalNanos = System.nanoTime() - startNanos;
		return new RequestLogEntry(
				"EVENT_LOOP",
				request.path(),
				properties.defaultAirline(),
				properties.defaultDate(),
				properties.defaultConcurrency(),
				null,
				null,
				nanosToMs(totalNanos),
				properties.payloadBytes(),
				Thread.currentThread().getName(),
				status
		);
	}

	private record DbResult(ReportWorkResult workResult, long dbNanos) {
	}
}
