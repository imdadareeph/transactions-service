package com.ia.vt.transactions_service.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class PerformanceLogger {
	private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceLogger.class);
	private static final String REQUEST_BORDER = "---------------------------------------------------------------------------------------------------------------";
	private static final String REQUEST_HEADER = "| PROF | MODEL          | ENDPOINT                     | AIRLN | DATE       | C | DB_MS | CPU_MS | TOTAL_MS | PAYLOAD | THREAD                  | ST |";
	private static final AtomicBoolean HEADER_LOGGED = new AtomicBoolean(false);

	private final BenchmarkMetrics metrics;
	private final String profileKey;
	private final String requestLabel;

	public PerformanceLogger(BenchmarkMetrics metrics, String profileKey, String requestLabel) {
		this.metrics = metrics;
		this.profileKey = profileKey;
		this.requestLabel = requestLabel;
	}

	@PostConstruct
	public void logHeader() {
		if (HEADER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info(REQUEST_BORDER);
			LOGGER.info(REQUEST_HEADER);
			LOGGER.info(REQUEST_BORDER);
		}
	}

	public void logRequest(RequestLogEntry entry) {
		String line = String.format(Locale.US,
				"| %-4s | %-14s | %-28s | %-5s | %-10s | %4s | %6s | %7s | %8s | %8s | %-24s | %4s |",
				requestLabel,
				entry.model(),
				trimToWidth(entry.endpoint(), 28),
				trimToWidth(entry.airline(), 5),
				trimToWidth(entry.reportDate(), 10),
				formatNumber(entry.concurrency()),
				formatNumber(entry.dbMs()),
				formatNumber(entry.cpuMs()),
				formatNumber(entry.totalMs()),
				formatNumber(entry.payloadBytes()),
				trimToWidth(entry.threadName(), 24),
				entry.status());
		LOGGER.info(line);
	}

	public void recordSuccess(RequestLogEntry entry, long startNanos, long endNanos, long latencyNanos) {
		metrics.recordSuccess(profileKey, startNanos, endNanos, latencyNanos, entry.payloadBytes() == null ? 0 : entry.payloadBytes(), entry.airline());
	}

	public void recordFailure(RequestLogEntry entry, long startNanos, long endNanos, long latencyNanos) {
		metrics.recordFailure(profileKey, startNanos, endNanos, latencyNanos, entry.payloadBytes() == null ? 0 : entry.payloadBytes(), entry.airline());
	}

	private String formatNumber(Number value) {
		return value == null ? "--" : String.valueOf(value);
	}

	private String trimToWidth(String value, int width) {
		if (value == null) {
			return "--";
		}
		if (value.length() <= width) {
			return value;
		}
		return value.substring(0, width);
	}
}
