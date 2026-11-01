#!/usr/bin/env sh
# Lightweight bootstrap launcher for this repository.
# It downloads the pinned Gradle distribution on first use, then executes it.
set -eu

GRADLE_VERSION="6.8.3"
CACHE_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/aetherium-bootstrap"
GRADLE_HOME="$CACHE_ROOT/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$CACHE_ROOT"
    ZIP="$CACHE_ROOT/gradle-$GRADLE_VERSION-bin.zip"
    URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    echo "Gradle $GRADLE_VERSION no está instalado; descargando distribución oficial..."
    if command -v curl >/dev/null 2>&1; then
        curl -fL "$URL" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$ZIP" "$URL"
    else
        echo "Se necesita curl o wget para descargar Gradle." >&2
        exit 1
    fi
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$ZIP" -d "$CACHE_ROOT"
    else
        echo "Se necesita unzip para extraer Gradle." >&2
        exit 1
    fi
    rm -f "$ZIP"
fi

exec "$GRADLE_BIN" "$@"
