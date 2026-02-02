package com.ia.vt.transactions_service.benchmark;

public record RequestLogEntry(
		String model,
		String endpoint,
		String airline,
		String reportDate,
		Integer concurrency,
		Long dbMs,
		Long cpuMs,
		Long totalMs,
		Integer payloadBytes,
		String threadName,
		String status
) {
}
