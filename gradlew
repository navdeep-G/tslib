#!/usr/bin/env sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPERTIES_FILE="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Missing $PROPERTIES_FILE" >&2
  exit 1
fi

get_property() {
  key="$1"
  sed -n "s/^${key}=//p" "$PROPERTIES_FILE" | tail -n 1 | sed 's#\\:#:#g'
}

DIST_URL=$(get_property distributionUrl)
if [ -z "$DIST_URL" ]; then
  echo "distributionUrl not found in $PROPERTIES_FILE" >&2
  exit 1
fi

DIST_ZIP=$(basename "$DIST_URL")
DIST_NAME=${DIST_ZIP%.zip}
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/$DIST_NAME"
GRADLE_HOME="$DIST_DIR/$DIST_NAME"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$DIST_DIR"
  TMP_ZIP="$DIST_DIR/$DIST_ZIP"
  if [ ! -f "$TMP_ZIP" ]; then
    echo "Downloading $DIST_URL"
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$DIST_URL" -o "$TMP_ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "$DIST_URL" -O "$TMP_ZIP"
    else
      echo "Neither curl nor wget is available to download Gradle." >&2
      exit 1
    fi
  fi
  rm -rf "$GRADLE_HOME"
  unzip -q -o "$TMP_ZIP" -d "$DIST_DIR"
fi

exec "$GRADLE_BIN" "$@"
