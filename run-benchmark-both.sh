#!/usr/bin/env bash
set -euo pipefail

REQUESTS_ARG="${1:-}"
if [[ -n "${REQUESTS_ARG}" ]]; then
  if [[ ! "${REQUESTS_ARG}" =~ ^[0-9]+$ ]]; then
    echo "Usage: $0 [requests]"
    exit 1
  fi
  REQUESTS="${REQUESTS_ARG}"
fi

JAVA_HOME="${JAVA_HOME:-}"
if [[ -z "${JAVA_HOME}" ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 22 2>/dev/null || true)"
fi
if [[ -z "${JAVA_HOME}" ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 22.0.1 2>/dev/null || true)"
fi
if [[ -z "${JAVA_HOME}" ]]; then
  echo "Java 22 not found. Set JAVA_HOME to a JDK 22 install or adjust the script."
  exit 1
fi

export JAVA_HOME
export PATH="${JAVA_HOME}/bin:${PATH}"

REQUESTS="${REQUESTS:-300}"
CONCURRENCY="${CONCURRENCY:-100}"
VT_PORT="${VT_PORT:-9191}"
WF_PORT="${WF_PORT:-9292}"
VT_ENDPOINT="${VT_ENDPOINT:-http://localhost:${VT_PORT}/reports/airline-booking}"
WF_ENDPOINT="${WF_ENDPOINT:-http://localhost:${WF_PORT}/reports/airline-booking}"
LOG_FILE="${LOG_FILE:-benchmark-summary.log}"
LOG_DIR="${LOG_DIR:-benchmark-logs}"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-180}"

SUMMARY_BORDER="================================================================================================"
SUMMARY_HEADER="| PROFILE          | AIRLINE | TOTAL_REQ | FAILED | AVG_MS | P95_MS | RPS   | TRANSFER_KB_SEC |"

mkdir -p "${LOG_DIR}"
APP_LOG="${LOG_DIR}/app.log"
: > "${APP_LOG}"

if lsof -i :"${VT_PORT}" -t >/dev/null 2>&1; then
  lsof -i :"${VT_PORT}" -t | xargs kill
fi
if lsof -i :"${WF_PORT}" -t >/dev/null 2>&1; then
  lsof -i :"${WF_PORT}" -t | xargs kill
fi

JAVA_HOME="${JAVA_HOME}" ./run-benchmark.sh >"${APP_LOG}" 2>&1 &
APP_PID=$!

waited=0
while true; do
  if ! kill -0 "${APP_PID}" 2>/dev/null; then
    echo "Application exited before startup."
    echo "Last 50 lines of ${APP_LOG}:"
    tail -n 50 "${APP_LOG}"
    exit 1
  fi
  if lsof -i :"${VT_PORT}" -t >/dev/null 2>&1 && lsof -i :"${WF_PORT}" -t >/dev/null 2>&1; then
    break
  fi
  sleep 1
  waited=$((waited + 1))
  if [[ "${waited}" -ge "${STARTUP_TIMEOUT}" ]]; then
    echo "Timed out waiting for both servers to start."
    kill "${APP_PID}" 2>/dev/null || true
    exit 1
  fi
done

ab -n "${REQUESTS}" -c "${CONCURRENCY}" "${VT_ENDPOINT}" || true
ab -n "${REQUESTS}" -c "${CONCURRENCY}" "${WF_ENDPOINT}" || true

kill "${APP_PID}" || true
wait "${APP_PID}" 2>/dev/null || true
sleep 1

cleanfile="$(mktemp)"
sed -E 's/\x1B\[[0-9;]*[A-Za-z]//g' "${APP_LOG}" > "${cleanfile}"
vt_row="$(grep -E "\| Virtual Threads " "${cleanfile}" | tail -1 | sed -E 's/^.*(\| Virtual Threads .*)/\1/' || true)"
wf_row="$(grep -E "\| Webflux " "${cleanfile}" | tail -1 | sed -E 's/^.*(\| Webflux .*)/\1/' || true)"
if [[ -z "${vt_row}" ]]; then
  vt_row="| Virtual Threads  | --      |         0 |      0 |      0 |      0 |   0.0 |              0.0 |"
fi
if [[ -z "${wf_row}" ]]; then
  wf_row="| Webflux          | --      |         0 |      0 |      0 |      0 |   0.0 |              0.0 |"
fi
rm -f "${cleanfile}"

{
  echo "${SUMMARY_BORDER}"
  echo "${SUMMARY_HEADER}"
  echo "${SUMMARY_BORDER}"
  [[ -n "${vt_row}" ]] && echo "${vt_row}"
  [[ -n "${wf_row}" ]] && echo "${wf_row}"
  echo "${SUMMARY_BORDER}"
} | tee "${LOG_FILE}"
