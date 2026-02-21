#!/bin/bash
set -euo pipefail

WRAPPER="${BUILD_WRAPPER:-$HOME/.local/bin/build-with-alert.sh}"

if [[ ! -x "$WRAPPER" ]]; then
  echo "Missing build wrapper: $WRAPPER" >&2
  exit 1
fi

"$WRAPPER" -C "$(cd "$(dirname "$0")" && pwd)" "$@"
