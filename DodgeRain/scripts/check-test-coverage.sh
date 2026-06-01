#!/usr/bin/env bash
# Verifies that every new/modified production .kt file has a corresponding
# test file, OR is exempt (UI/Activity layer that can't be JVM-tested).
#
# CI usage: scripts/check-test-coverage.sh

set -euo pipefail

SRC_DIR="DodgeRain/app/src/main/java/com/example/dodgerain"
TEST_DIR="DodgeRain/app/src/test/java/com/example/dodgerain"

# Files matching these patterns are exempt (Android-framework-coupled UI classes)
# Matches: *Activity.kt, *View.kt, *Renderer.kt (and their subclasses, e.g. MainGameActivity.kt)
EXEMPT_PATTERNS=(
    "*Activity.kt"
    "*View.kt"
    "*Renderer.kt"
)

is_exempt() {
    local file="$1"
    for pattern in "${EXEMPT_PATTERNS[@]}"; do
        # shellcheck disable=SC2254
        case "$file" in
            $pattern) return 0 ;;
        esac
    done
    return 1
}

# Get changed .kt files in main/
changed_files=$(git diff --name-only origin/main...HEAD 2>/dev/null \
    | grep -E "^${SRC_DIR}/.*\.kt$" || true)

if [[ -z "$changed_files" ]]; then
    echo "✓ No production Kotlin changes — test coverage check skipped"
    exit 0
fi

failed=0
while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    basename=$(basename "$file" .kt)
    if is_exempt "${basename}.kt"; then
        echo "⊘ ${basename}.kt — exempt (Android-coupled, not unit-testable)"
        continue
    fi
    expected_test="${TEST_DIR}/${basename}Test.kt"
    if [[ -f "$expected_test" ]]; then
        echo "✓ ${basename}.kt has ${basename}Test.kt"
    else
        echo "❌ ${basename}.kt is missing ${basename}Test.kt"
        failed=1
    fi
done <<< "$changed_files"

if [[ "$failed" == "1" ]]; then
    cat <<EOF

This codebase requires unit tests for all changes in $SRC_DIR.
If a class genuinely cannot be unit-tested (Android framework dependency),
add its filename to EXEMPT_FILES in this script with justification.

See CLAUDE.md "Hard Rules" for the testing requirement.
EOF
    exit 1
fi
