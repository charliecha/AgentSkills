<!--
Fill out every section. CI will reject PRs that skip the Spec Reference
or Test Coverage sections. Reviewers will reject PRs with empty checklist.
-->

## What & Why

<!-- One paragraph: what changes, why it matters. Link to issue if applicable. -->

## Spec Reference

<!-- Pick ONE: -->
- [ ] Spec: `docs/spec/<feature>.md`
- [ ] Plan: `docs/plan/<feature>.md`
- [ ] `no-spec-needed: <one-line reason, e.g. "typo fix in README">`

## Workflow Checklist

- [ ] `/spec-driven-development` ran (or no-spec-needed declared)
- [ ] `/planning-and-task-breakdown` ran (or change is single-task)
- [ ] `/test-driven-development` ran — new behaviors have failing-then-passing tests
- [ ] `/code-review-and-quality` self-review pass complete
- [ ] All commits follow `type:` prefix convention

## Test Evidence

<!-- Paste output of: ./gradlew testDebugUnitTest -->
```
N tests completed, 0 failed
```

## Manual Verification (if UI changed)

<!-- Screenshots from emulator showing each affected state.
     Or write: "no UI change" -->

## Out of Scope (Optional)

<!-- Things you noticed but intentionally did NOT touch.
     Helps reviewer trust your scope discipline. -->

## Rollback Plan

<!-- One line: how do we revert if this breaks production?
     For most PRs: "git revert <hash>" is fine. -->
