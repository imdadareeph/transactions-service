#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${JAVA_HOME:-}" ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 22 2>/dev/null || true)"
fi
if [[ -z "${JAVA_HOME:-}" ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 22.0.1 2>/dev/null || true)"
fi
if [[ -z "${JAVA_HOME:-}" ]]; then
  echo "Java 22 not found. Set JAVA_HOME to a JDK 22 install or adjust the script."
  exit 1
fi

export JAVA_HOME
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "Starting transactions-service (Virtual Threads on 9191, Webflux on 9292) JAVA_HOME=${JAVA_HOME}"
exec ./mvnw spring-boot:run
