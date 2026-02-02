package com.ia.vt.transactions_service.model;

import java.time.LocalDate;

public record ReportResponse(
		String airline,
		LocalDate reportDate,
		long dbTimeMs,
		long cpuTimeMs,
		long totalTimeMs,
		int payloadSize,
		String threadName,
		String threadType,
		String payload
) {
}
