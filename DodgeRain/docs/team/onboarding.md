# DodgeRain — Onboarding Guide

## Setup (5 minutes)

### 1. Prerequisites

- Android Studio (latest stable)
- JDK 21: `brew install --cask temurin@21`
- Claude Code: `npm install -g @anthropic-ai/claude-code`
- agent-skills: `git clone https://github.com/addyosmani/agent-skills ~/.claude/skills/agent-skills && cp -r ~/.claude/skills/agent-skills/skills/* ~/.claude/skills/`

### 2. Clone & Configure

```bash
git clone <repo-url>
cd AgentSkills/DodgeRain

# Install local git hooks (REQUIRED)
bash scripts/install-hooks.sh

# Set JAVA_HOME (add to your shell profile)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
```

### 3. Verify Setup

```bash
./gradlew testDebugUnitTest   # Should print: 30 tests completed, 0 failed
./gradlew assembleDebug       # Should print: BUILD SUCCESSFUL
```

---

## Daily Workflow

**The rule:** Any change touching `app/src/main/` requires the full workflow.

```
Claude Code → /spec-driven-development
           → /planning-and-task-breakdown
           → /incremental-implementation
           → /test-driven-development
           → /code-review-and-quality
           → /git-workflow-and-versioning
           → open PR (use template)
```

**Skipping is allowed only for:**
- Typo / comment fixes
- Pure refactoring under 20 lines, no behavior change
- Bug fixes ≤ 3 lines — still requires a regression test

When skipping, write `no-spec-needed: <reason>` in the PR description.

---

## Commit Convention

Format: `<type>: <subject>` (max 72 chars)

| Type | When |
|---|---|
| `feat` | New behavior |
| `fix` | Bug fix |
| `refactor` | Code change with no behavior change |
| `test` | Adding or updating tests |
| `docs` | Documentation only |
| `chore` | Build, deps, config |
| `perf` | Performance improvement |

The `commit-msg` hook enforces this. Invalid format → commit rejected.

---

## Opening a Pull Request

1. Push your branch
2. Open PR on GitHub — the template auto-loads
3. Fill every section (empty checklist = reviewer rejects)
4. CI runs automatically:
   - `workflow-compliance`: commit messages, spec reference, test coverage
   - `lint`: static analysis
   - `test`: 30 unit tests
   - `build`: debug + release APK

**All CI jobs must be green before requesting review.**

---

## Reviewer Guide

When reviewing a PR, check in this order:

### 1. Workflow (before reading code)
- [ ] PR description is filled out (not just the template skeleton)
- [ ] Spec referenced (`docs/spec/`) or `no-spec-needed` declared with reason
- [ ] Workflow checklist checked — not just rubber-stamped
- [ ] Test output pasted (shows tests actually ran)

**If any of these are missing → Request Changes immediately. Don't review code yet.**

### 2. Architecture Boundaries
- [ ] `GameEngine.kt` has zero Android framework imports (`Canvas`, `RectF`, `Context`)
- [ ] `GameView.kt` has zero game logic — only input forwarding and lifecycle
- [ ] New classes follow the existing dependency direction in CLAUDE.md

### 3. Test Quality
- [ ] New behaviors have tests that would have caught the bug if written first
- [ ] Test names describe behavior, not implementation (`moves to left boundary`, not `test1`)
- [ ] No tests deleted or `@Ignore`-d without explanation

### 4. Code (Five Axes from `code-review-and-quality`)
- [ ] Correctness: edge cases, error paths, no off-by-one
- [ ] Readability: names are clear, no magic numbers, logic is followable
- [ ] Architecture: fits existing patterns, no new abstractions without 3+ use cases
- [ ] Security: no hardcoded values, no user-input without validation
- [ ] Performance: no allocations in the game loop hot path

### 5. Verdict Labels

| Label | Meaning |
|---|---|
| *(no prefix)* | Must fix before merge |
| `Nit:` | Optional, author may ignore |
| `Consider:` | Worth thinking about, not required |
| `FYI:` | No action needed |

---

## Branch Protection Settings (GitHub)

Set on `main` branch (repo → Settings → Branches → Add rule):

- [x] **Require a pull request before merging**
  - Required approvals: **1**
- [x] **Require status checks to pass before merging**
  - Required checks: `workflow-compliance`, `lint`, `test`, `build`
- [x] **Require branches to be up to date before merging**
- [x] **Do not allow bypassing the above settings**
- [ ] Force pushes: **disabled**
- [ ] Deletions: **disabled**

---

## FAQ

**Q: CI says my commit message is invalid. What format?**
A: `feat: add player movement` — type colon space description. No period at end.

**Q: CI says I need a spec but it's a 2-line bug fix.**
A: Add `no-spec-needed: bug fix, < 5 lines` to the PR description.

**Q: I disagree with a review comment. What do I do?**
A: Reply with your reasoning. If the reviewer's concern is valid, fix it. If you disagree after discussion, a third team member resolves. Don't silently ignore.

**Q: CI is flaky — test passed locally but failed in CI.**
A: Re-run once. If it fails again, investigate before asking for a bypass. Flaky tests are bugs.
