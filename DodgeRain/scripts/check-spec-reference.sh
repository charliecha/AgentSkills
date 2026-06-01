#!/usr/bin/env bash
# Verifies that PRs touching production code either reference a spec
# or explicitly declare why no spec is needed.
#
# CI usage:
#   PR_BODY="$(cat pr_body.txt)" scripts/check-spec-reference.sh

set -euo pipefail

PR_BODY="${PR_BODY:-}"

if [[ -z "$PR_BODY" ]]; then
    echo "⚠️  PR_BODY env var not set — skipping spec reference check"
    exit 0
fi

# Detect if any production code changed
if ! git diff --name-only origin/main...HEAD 2>/dev/null | grep -qE '^DodgeRain/app/src/main/.*\.(kt|xml)$'; then
    echo "✓ No production code changes — spec reference not required"
    exit 0
fi

# Check for spec reference or no-spec-needed declaration
if echo "$PR_BODY" | grep -qE 'docs/spec/.*\.md|docs/plan/.*\.md|no-spec-needed:'; then
    echo "✓ PR references a spec/plan or declares no-spec-needed"
    exit 0
fi

cat <<EOF
❌ This PR changes production code but has no spec reference.

Either:
  1. Add a link to docs/spec/<feature>.md or docs/plan/<feature>.md
  2. Add 'no-spec-needed: <reason>' to the PR description

This requirement exists to prevent ad-hoc changes that bypass design review.
See CLAUDE.md for the required workflow.
EOF
exit 1
