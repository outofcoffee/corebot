#!/usr/bin/env bash
set -e

CURRENT_BRANCH="$( git branch --show-current )"

if [[ "$CURRENT_BRANCH" == "develop" || "$CURRENT_BRANCH" == "master" || "$CURRENT_BRANCH" == "main" ]]; then
  EFFECTIVE_BRANCH_NAME="$CURRENT_BRANCH"

else
  case "$( git describe --tags --exact-match 2>/dev/null )" in
  v1.*)
    EFFECTIVE_BRANCH_NAME="main"
    ;;
  *)
    EFFECTIVE_BRANCH_NAME="dev"
    ;;
  esac
fi

echo "$EFFECTIVE_BRANCH_NAME"
