package com.ia.vt.transactions_service.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Locale;

@Component
public class CombinedSummaryLogger {
	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedSummaryLogger.class);
	private static final String SUMMARY_BORDER = "================================================================================================";
	private static final String SUMMARY_HEADER = "| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |";

	private final BenchmarkMetrics metrics;

	public CombinedSummaryLogger(BenchmarkMetrics metrics) {
		this.metrics = metrics;
	}

	@PreDestroy
	public void logSummary() {
		LOGGER.info(SUMMARY_BORDER);
		LOGGER.info(SUMMARY_HEADER);
		LOGGER.info(SUMMARY_BORDER);
		logProfile("Virtual Threads");
		logProfile("Webflux");
		LOGGER.info(SUMMARY_BORDER);
	}

	private void logProfile(String profile) {
		BenchmarkSummary summary = metrics.snapshotSummary(profile);
		String line = String.format(Locale.US,
				"| %-16s | %-7s | %9d | %6d | %6d | %6d | %5.1f | %16.1f |",
				summary.profile(),
				summary.airline(),
				summary.totalRequests(),
				summary.failedRequests(),
				summary.avgLatencyMs(),
				summary.p95LatencyMs(),
				summary.requestsPerSecond(),
				summary.transferKbSec());
		LOGGER.info(line);
	}
}
