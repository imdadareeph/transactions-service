package com.ia.vt.transactions_service.benchmark;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

@Component
public class BenchmarkMetrics {
	private final Map<String, ProfileMetrics> metricsByProfile = new ConcurrentHashMap<>();

	public void recordSuccess(String profile, long startNanos, long endNanos, long latencyNanos, int payloadBytes, String airlineCode) {
		metricsFor(profile).recordRequest(startNanos, endNanos, latencyNanos, payloadBytes, airlineCode, false);
	}

	public void recordFailure(String profile, long startNanos, long endNanos, long latencyNanos, int payloadBytes, String airlineCode) {
		metricsFor(profile).recordRequest(startNanos, endNanos, latencyNanos, payloadBytes, airlineCode, true);
	}

	public BenchmarkSummary snapshotSummary(String profile) {
		ProfileMetrics metrics = metricsByProfile.get(profile);
		if (metrics == null) {
			return new BenchmarkSummary(profile, "--", 0L, 0L, 0L, 0L, 0.0, 0.0);
		}
		return metrics.snapshotSummary(profile);
	}

	private ProfileMetrics metricsFor(String profile) {
		return metricsByProfile.computeIfAbsent(profile, ignored -> new ProfileMetrics());
	}

	private static class ProfileMetrics {
		private final LongAdder totalRequests = new LongAdder();
		private final LongAdder failedRequests = new LongAdder();
		private final LongAdder totalLatencyNanos = new LongAdder();
		private final LongAdder totalBytes = new LongAdder();
		private final ConcurrentLinkedQueue<Long> latenciesNanos = new ConcurrentLinkedQueue<>();
		private final AtomicLong firstRequestStartNanos = new AtomicLong(-1);
		private final AtomicLong lastRequestEndNanos = new AtomicLong(-1);
		private final AtomicReference<String> airline = new AtomicReference<>();

		private void recordRequest(long startNanos, long endNanos, long latencyNanos, int payloadBytes, String airlineCode, boolean failed) {
			if (failed) {
				failedRequests.increment();
			}
			totalRequests.increment();
			totalLatencyNanos.add(latencyNanos);
			totalBytes.add(payloadBytes);
			latenciesNanos.add(latencyNanos);
			updateFirstStart(startNanos);
			updateLastEnd(endNanos);
			updateAirline(airlineCode);
		}

		private void updateFirstStart(long startNanos) {
			firstRequestStartNanos.compareAndSet(-1, startNanos);
		}

		private void updateLastEnd(long endNanos) {
			long prev;
			do {
				prev = lastRequestEndNanos.get();
				if (endNanos <= prev) {
					return;
				}
			} while (!lastRequestEndNanos.compareAndSet(prev, endNanos));
		}

		private void updateAirline(String airlineCode) {
			airline.updateAndGet(existing -> {
				if (existing == null) {
					return airlineCode;
				}
				if (existing.equals(airlineCode)) {
					return existing;
				}
				return "MIX";
			});
		}

		private BenchmarkSummary snapshotSummary(String profile) {
			long total = totalRequests.sum();
			long failed = failedRequests.sum();
			long totalLatency = totalLatencyNanos.sum();
			long bytes = totalBytes.sum();
			long start = firstRequestStartNanos.get();
			long end = lastRequestEndNanos.get();

			double durationSeconds = start > 0 && end > 0 && end > start
					? (end - start) / 1_000_000_000.0
					: 0.0;
			double rps = durationSeconds > 0 ? total / durationSeconds : 0.0;
			double transferKbSec = durationSeconds > 0 ? (bytes / 1024.0) / durationSeconds : 0.0;

			long avgMs = total > 0 ? nanosToMs(totalLatency / total) : 0L;
			long p95Ms = total > 0 ? nanosToMs(percentileNanos(0.95)) : 0L;

			String airlineCode = airline.get() != null ? airline.get() : "--";
			return new BenchmarkSummary(profile, airlineCode, total, failed, avgMs, p95Ms, rps, transferKbSec);
		}

		private long percentileNanos(double percentile) {
			List<Long> values = new ArrayList<>(latenciesNanos);
			if (values.isEmpty()) {
				return 0L;
			}
			Collections.sort(values);
			int index = (int) Math.ceil(percentile * values.size()) - 1;
			index = Math.min(Math.max(index, 0), values.size() - 1);
			return values.get(index);
		}

		private long nanosToMs(long nanos) {
			return nanos / 1_000_000L;
		}
	}
}
