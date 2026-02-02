# Benchmark Command Explanation

## Command
`REQUESTS=1500 CONCURRENCY=500 ./run-benchmark-both.sh`

## What it does
- `REQUESTS=1500` tells the script to run 1500 total HTTP requests per endpoint (Virtual Threads on port 9191, Webflux on port 9292).
- `CONCURRENCY=500` tells ApacheBench (`ab`) to open 500 concurrent connections at a time.
- `./run-benchmark-both.sh` starts the single JVM app (dual stack), waits for both ports, runs `ab` against both endpoints, then shuts down the app and prints a combined summary table.

## Output structure
1. ApacheBench output for port 9191 (Virtual Threads)
2. ApacheBench output for port 9292 (Webflux)
3. Combined summary table emitted by the app at shutdown:

```
| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |
| Virtual Threads  | SQ      |        26 |      0 |    605 |    606 |  18.7 |             18.7 |
| Webflux          | SQ      |       546 |      0 |    601 |    605 | 261.2 |            261.2 |
```

## Notes on ApacheBench "Failed requests"
- `ab` reports "Failed requests: Length" when response payload size differs.
- This does not indicate application errors; it reflects variable response sizes (for example, timing fields in the JSON).
- The app summary table's `FAILED` column reflects actual request failures seen by the app (typically 0).

## Why use 1500 requests and 500 concurrency
- Higher concurrency stresses scheduling and event loops.
- More total requests stabilizes averages and reduces noise.
- Makes subtle differences between Virtual Threads and Webflux easier to observe.
