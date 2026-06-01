# DodgeRain — Engineering Workflow

> This file is auto-loaded by Claude Code. It defines how work flows in this repo.
> Skipping these steps is not a shortcut — it is technical debt with interest.

## Required Workflow

All non-trivial changes (more than ~10 lines, or any new behavior) MUST follow this sequence:

```
1. /spec-driven-development     → docs/spec/<feature>.md
2. /planning-and-task-breakdown  → docs/plan/<feature>.md
3. /incremental-implementation   → code in app/src/main/
4. /test-driven-development      → tests in app/src/test/
5. /code-review-and-quality      → self-review before PR
6. /git-workflow-and-versioning  → atomic commits
```

**Exceptions (skip the workflow):**
- Typo / comment fixes
- Pure refactoring under 20 lines with no behavior change
- Bug fixes ≤ 3 lines (still requires a regression test)

When skipping, declare it in the PR description: `no-spec-needed: <one-line reason>`.

## Hard Rules

| Rule | Enforcement |
|---|---|
| New code in `app/src/main/` must have matching `*Test.kt` in `app/src/test/` | CI (`scripts/check-test-coverage.sh`) |
| Commit messages must use `feat:` / `fix:` / `refactor:` / `test:` / `docs:` / `chore:` / `perf:` prefix | commitlint hook |
| PR must reference a spec OR declare `no-spec-needed: <reason>` | CI (`scripts/check-spec-reference.sh`) |
| No `RectF.intersects` or other Android-framework calls in `GameEngine.kt` | Code review (logic must stay JVM-testable) |
| `GameView` only handles input + lifecycle, no game logic | Code review |

## Architecture Boundaries

```
MainActivity ──→ GameView ──→ GameEngine ──→ Player / Obstacle
                     ↓              ↑
                GameRenderer ───────┘
```

- `GameEngine` is the only place game state lives. It must remain free of Android framework dependencies (no `Canvas`, `RectF`, `Context`) so it stays unit-testable.
- `GameRenderer` reads from `GameEngine`, draws to `Canvas`. Never mutates engine state.
- `GameView` runs the game loop on its own thread. Touch events forward to `GameEngine.onTouchX()` — no logic in handlers.

## Local Commands

```bash
# From DodgeRain/
./gradlew testDebugUnitTest    # run 30 unit tests
./gradlew lint                 # static analysis
./gradlew assembleDebug        # debug APK
./gradlew installDebug         # install to connected device
```

Set `JAVA_HOME` to a JDK 21 install before running:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
```

## When in Doubt

- New feature → start with `/spec-driven-development`
- Stuck → start with `/interview-me` to clarify intent
- Unsure if a change is in scope → ask in PR before coding, not after

The skills are not suggestions. They are how this codebase stays maintainable as it grows.
