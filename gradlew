#!/bin/bash
# Arena Checkpoint Gradle entry point.
# Restores gradle/wrapper/gradle-wrapper.jar from the ASCII sidecar when that
# file is present. Otherwise downloads the NeoForge 1.21.1 MDK wrapper jar and
# checks its SHA-256 before launching Gradle.
set -euo pipefail

APP_HOME=$(cd "$(dirname "$0")" && pwd)
wrapper_jar="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
wrapper_b64="$APP_HOME/gradle/wrapper/gradle-wrapper.jar.base64"
wrapper_sha="7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172"
wrapper_url="https://raw.githubusercontent.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/main/gradle/wrapper/gradle-wrapper.jar"
wrapper_restored=false

if [ ! -s "$wrapper_jar" ]; then
    wrapper_restored=true
    if [ -f "$wrapper_b64" ]; then
        if ! base64 -d "$wrapper_b64" > "$wrapper_jar" 2>/dev/null; then
            base64 -D -o "$wrapper_jar" "$wrapper_b64"
        fi
    elif command -v curl >/dev/null 2>&1; then
        curl -fsSL "$wrapper_url" -o "$wrapper_jar"
    else
        wget -q -O "$wrapper_jar" "$wrapper_url"
    fi
fi

if command -v sha256sum >/dev/null 2>&1; then
    wrapper_actual=$(sha256sum "$wrapper_jar" | awk '{print $1}')
else
    wrapper_actual=$(shasum -a 256 "$wrapper_jar" | awk '{print $1}')
fi
if [ "$wrapper_actual" != "$wrapper_sha" ]; then
    if [ "$wrapper_restored" = true ]; then
        rm -f "$wrapper_jar"
    fi
    echo "ERROR: Gradle wrapper jar hash mismatch" >&2
    exit 1
fi

if [ -n "${JAVA_HOME:-}" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" "-Dorg.gradle.appname=${0##*/}" -jar "$wrapper_jar" "$@"
