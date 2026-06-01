#!/usr/bin/env bash
# Git hooks installer. Run once per clone:
#   bash scripts/install-hooks.sh
#
# Installs hooks that mirror CI checks so failures happen locally first.

set -euo pipefail

REPO_ROOT=$(git rev-parse --show-toplevel)
HOOKS_DIR="$REPO_ROOT/.git/hooks"
SCRIPTS_DIR="$REPO_ROOT/DodgeRain/scripts"

if [[ ! -d "$SCRIPTS_DIR" ]]; then
    echo "❌ Expected $SCRIPTS_DIR to exist. Run from repo root."
    exit 1
fi

# commit-msg: validate format
cat > "$HOOKS_DIR/commit-msg" <<EOF
#!/usr/bin/env bash
exec bash "$SCRIPTS_DIR/check-commit-message.sh" "\$1"
EOF
chmod +x "$HOOKS_DIR/commit-msg"
echo "✓ Installed commit-msg hook"

# pre-push: run unit tests
cat > "$HOOKS_DIR/pre-push" <<'EOF'
#!/usr/bin/env bash
set -e
echo "Running unit tests before push..."
cd "$(git rev-parse --show-toplevel)/DodgeRain"
if [[ -z "${JAVA_HOME:-}" ]]; then
    echo "⚠️  JAVA_HOME not set — skipping local tests. CI will still run them."
    exit 0
fi
./gradlew testDebugUnitTest --quiet || {
    echo ""
    echo "❌ Unit tests failed. Fix them before pushing."
    echo "   Bypass (NOT recommended): git push --no-verify"
    exit 1
}
EOF
chmod +x "$HOOKS_DIR/pre-push"
echo "✓ Installed pre-push hook"

echo ""
echo "Hooks installed. CI checks now run locally on commit and push."
