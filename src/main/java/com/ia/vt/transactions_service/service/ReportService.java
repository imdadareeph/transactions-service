package com.ia.vt.transactions_service.service;

import com.ia.vt.transactions_service.model.ReportRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ReportService {
	public ReportWorkResult generateReport(ReportRequest request, int cpuIterations) {
		long cpuStart = System.nanoTime();
		byte[] baseBytes = (request.airline() + "|" + request.reportDate() + "|" + request.payloadBytes())
				.getBytes(StandardCharsets.US_ASCII);
		byte[] hash = computeDeterministicHash(baseBytes, cpuIterations);
		long cpuElapsed = System.nanoTime() - cpuStart;

		String payload = buildPayload(request.payloadBytes(), hash);
		return new ReportWorkResult(cpuElapsed, payload, request.payloadBytes());
	}

	private byte[] computeDeterministicHash(byte[] baseBytes, int cpuIterations) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = baseBytes;
			for (int i = 0; i < cpuIterations; i++) {
				digest.update(hash);
				hash = digest.digest(baseBytes);
			}
			return hash;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	private String buildPayload(int payloadBytes, byte[] hash) {
		if (payloadBytes <= 0) {
			return "";
		}
		char[] payload = new char[payloadBytes];
		for (int i = 0; i < payloadBytes; i++) {
			payload[i] = (char) ('A' + (hash[i % hash.length] & 0x0F));
		}
		return new String(payload);
	}
}
