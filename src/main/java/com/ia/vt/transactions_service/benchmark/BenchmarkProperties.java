package com.ia.vt.transactions_service.benchmark;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "benchmark")
public record BenchmarkProperties(
		String defaultAirline,
		String defaultDate,
		int defaultConcurrency,
		int payloadBytes,
		int dbDelayMs,
		int cpuIterations
) {
}
