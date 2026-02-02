package com.ia.vt.transactions_service.benchmark;

public record BenchmarkSummary(
		String profile,
		String airline,
		long totalRequests,
		long failedRequests,
		long avgLatencyMs,
		long p95LatencyMs,
		double requestsPerSecond,
		double transferKbSec
) {
}
