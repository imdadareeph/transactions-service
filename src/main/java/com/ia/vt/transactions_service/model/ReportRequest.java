package com.ia.vt.transactions_service.model;

import java.time.LocalDate;

public record ReportRequest(
		String airline,
		LocalDate reportDate,
		int concurrency,
		int payloadBytes
) {
}
