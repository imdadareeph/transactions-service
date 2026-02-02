# Benchmark Report

This report summarizes benchmark behavior across different payload sizes and documents the canonical log table layout.

------------------------------------------------------------
## Payload sizes
Payload size is controlled by `benchmark.payload-bytes` (default: 1024) and can be overridden per request with the `payload` query parameter.

Example payload sizes to evaluate:
- 256 bytes: `http://localhost:9191/reports/airline-booking?payload=256`
- 1024 bytes: `http://localhost:9191/reports/airline-booking?payload=1024`
- 4096 bytes: `http://localhost:9191/reports/airline-booking?payload=4096`
- 16384 bytes: `http://localhost:9191/reports/airline-booking?payload=16384`

For each payload size, run the benchmark against both ports:
- Virtual Threads: `http://localhost:9191/reports/airline-booking?payload=...`
- Webflux: `http://localhost:9292/reports/airline-booking?payload=...`

------------------------------------------------------------
## Benchmark results (REQUESTS=1500, CONCURRENCY=500)
Payload size is set via the `payload` query parameter.

Payload 256 bytes:
```
| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |
| Virtual Threads  | SQ      |       663 |      0 |    602 |    605 | 313.3 |             78.3 |
| Webflux          | SQ      |       501 |      0 |    601 |    605 | 350.0 |             87.5 |
```

Payload 1024 bytes:
```
| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |
| Virtual Threads  | SQ      |       643 |      0 |    602 |    605 | 302.4 |            302.4 |
| Webflux          | SQ      |       501 |      0 |    602 |    606 | 347.3 |            347.3 |
```

Payload 4096 bytes:
```
| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |
| Virtual Threads  | SQ      |       152 |      0 |    621 |    629 | 105.4 |            421.6 |
| Webflux          | SQ      |       501 |      0 |    601 |    605 | 356.1 |           1424.4 |
```

Payload 16384 bytes:
```
| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |
| Virtual Threads  | SQ      |       571 |      0 |    610 |    623 | 273.6 |           4377.5 |
| Webflux          | SQ      |       501 |      0 |    611 |    622 | 347.8 |           5564.6 |
```

