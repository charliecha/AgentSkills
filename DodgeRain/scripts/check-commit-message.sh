#!/usr/bin/env bash
# Validates commit messages against conventional format:
#   <type>: <subject>
# where <type> is one of: feat, fix, refactor, test, docs, chore, perf
#
# Usage:
#   scripts/check-commit-message.sh <commit-msg-file-or-string>
# Or read from git rev (CI mode):
#   scripts/check-commit-message.sh --range origin/main..HEAD

set -euo pipefail

ALLOWED_TYPES="feat|fix|refactor|test|docs|chore|perf"
PATTERN="^(${ALLOWED_TYPES})(\([a-z0-9-]+\))?: .{1,72}"

check_message() {
    local msg="$1"
    local first_line
    first_line=$(echo "$msg" | head -n1)

    # Skip merge commits
    if [[ "$first_line" =~ ^Merge ]]; then
        return 0
    fi

    if [[ ! "$first_line" =~ $PATTERN ]]; then
        echo "❌ Invalid commit message:"
        echo "   $first_line"
        echo ""
        echo "Expected format: <type>: <subject>"
        echo "Allowed types: feat, fix, refactor, test, docs, chore, perf"
        echo "Example: feat: add task creation endpoint"
        return 1
    fi
    return 0
}

if [[ "${1:-}" == "--range" ]]; then
    range="${2:?usage: --range <ref>..<ref>}"
    failed=0
    while IFS= read -r sha; do
        msg=$(git log -1 --format=%B "$sha")
        if ! check_message "$msg"; then
            echo "   commit: $sha"
            failed=1
        fi
    done < <(git rev-list "$range")
    exit "$failed"
else
    # Read from file (git hook mode) or arg
    if [[ -f "${1:-}" ]]; then
        msg=$(cat "$1")
    else
        msg="${1:-}"
    fi
    check_message "$msg"
fi
