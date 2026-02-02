package com.ia.vt.transactions_service.service;

public record ReportWorkResult(
		long cpuTimeNanos,
		String payload,
		int payloadSize
) {
}
