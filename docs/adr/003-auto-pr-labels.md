# ADR-003: Automated PR Label Assignment

## Status
✅ Approved - Workflow created and validated

## Context
To standardize label assignment across all pull requests in the Java_Platformer repository, we need to automatically apply consistent labels based on PR title patterns (e.g., `feat(user-auth)`, `fix(database)`, `docs`). Manual labeling is prone to inconsistency and delays review processes.

### Problem Statement
- **Inconsistent labeling**: Contributors use varying conventions when tagging issues in PR titles
- **Delayed visibility**: Without auto-labels, important label types (`test`, `chore`) may be missed
- **Review friction**: Reviewers must manually identify PR categories before assessing changes
- **No governance**: Clear expectations around what constitutes a "fix" vs "feature"

### Label Definitions

| Label | Description | Example Titles |
|-------|-------------|----------------|
| `feat` | New feature | `feat(logic)`, `feat(input)` |
| `fix` | Bug fix | `fix(user-auth)`, `fix(database)` |
| `chore` | Maintenance/infrastructure | `chore(deps)`, `chore(build)` |
| `test` | Test changes | `test(core)`, `test(ui)` |
| `docs` | Documentation | `docs(api)`, `docs(readme)` |

## Decision
Create a GitHub Actions workflow that automatically assigns labels based on PR title patterns matching the format `<type>(<component>)`. The workflow triggers on PR creation, updates, and label changes.

### Implementation Details

1. **Trigger Events**: `pull_request` with types `opened`, `edited`, `synchronize`
2. **Pattern Matching**: Regex detection for `feat:()`, `fix:()`, `chore:()`, `test:()`, `docs:()` in PR title
3. **Permissions Required**: `issues: write` to apply labels
4. **Fallback**: If no recognized pattern is found, add `needs-label` label prompting contributor to review the title format

## Impact Analysis

### Benefits
- ✅ Standardized workflow across all contributors
- ✅ Faster triage for maintainers
- ✅ Consistent categorization for release notes and documentation
- ✅ Reduces duplicate manual labeling

### Considerations
- Contributors must format PR titles as `<type>(<component>)`
- Manual override is still available via the "Edit" button in GitHub UI
- Workflow runs on PR update (`edited` event) to fix missing labels

## Related
- [[adr002-test-simplification]] - User preference for simplified test patterns
- [[auto-labeling-config]] - Label configuration reference

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
