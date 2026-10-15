# ADR-005: Changelog Automation

## Status
✅ Approved - Workflow created and validated

## Context
To maintain a transparent, version-controlled record of all changes made to Java_Platformer, we need a changelog that tracks merged PRs without manual maintenance. Without automation, the CHANGELOG.md would require constant updates, increasing the risk of omissions and becoming a source of friction for contributors.

### Problem Statement
- **Manual maintenance burden**: Keeping CHANGELOG.md up-to-date requires manual intervention after every PR merge
- **Contributor fatigue**: Contributors must remember to update changelog entries themselves
- **Risk of omission**: Important changes may be overlooked in documentation
- **Version tracking gaps**: Breaking or significant features may not be properly documented

### Benefits of Automated Changelog
| Benefit | Impact |
|---------|--------|
| Automatic PR-to-changelog ingestion | Eliminates manual work after each PR merge |
| Consistent formatting via template | Standardized entries across all releases |
| Reduced contributor friction | One less thing contributors need to do |
| Always-current documentation | CHANGELOG.md reflects the latest merged changes immediately |

## Decision
Create a GitHub Actions workflow that automatically generates and maintains `CHANGELOG.md` from merged PRs using:

1. **Automated generation**: Run after each PR merge to `main`
2. **Template-based output**: Follow Keep a Changelog format ([Keep a Changelog](https://keepachangelog.com/en/1.0.0/))
3. **Separate PR creation**: Generate an auto-review PR from `changelog/auto` branch
4. **Human gate**: Require manual review and merge (no auto-merge)

### Implementation Details

**Files Created:**
- `.github/workflows/changelog.yml` - CI workflow that triggers on main branch pushes
- `scripts/gen-changelog.sh` - Bash script that generates changelog entries from PRs
- `CHANGELOG.md` - Target document being updated by the workflow

**Workflow Behavior:**
```yaml
on:
  push:
    branches: [main]
    paths-ignore:
      - CHANGELOG.md  # Prevent retriggering on changelog updates only

jobs:
  changelog:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Full history for PR fetching
          token: ${{ secrets.CHANGELOG_PAT }}
      
      - run: ./scripts/gen-changelog.sh
        
      - name: Open/update changelog PR
        run: |
          gh pr create --base main --head "changelog/auto" \
            --title "docs: update CHANGELOG.md"
```

**Authentication:**
- Requires secret `CHANGELOG_PAT` (not default GITHUB_TOKEN)
- PAT scope: `repo: full_control` or at minimum `contents: write` + `pull_requests: write`
- **Reason**: GitHub's default GITHUB_TOKEN does not trigger CI on its own changes

### Gen-Changelog Script Flow

1. Fetch merged PRs from main branch (`gh pr list --base main --state merged`)
2. Parse each PR for title and body content
3. Generate changelog entries in format:
   ```markdown
   ## vX.Y.Z
   
   * #123: feature(description) by <author>
   ```
4. Create temporary `changelog/auto` branch with new CHANGELOG.md
5. Open/update a PR for human review and merge

## Impact Analysis

### Benefits
- ✅ Zero manual changelog maintenance after initial setup
- ✅ Consistent, versioned documentation across all releases
- ✅ Clear audit trail of what changes were documented when
- ✅ Contributors focus on code, not docs upkeep

### Considerations
- **PAT management**: Requires setting up and maintaining a separate secret (`CHANGELOG_PAT`)
  - Should have `repo` scope with write permissions
  - Not equivalent to the default GITHUB_TOKEN used in other workflows
  
- **Human review required**: No auto-merge means:
  - Human must manually merge the generated PR
  - Bot cannot make direct changes to main
  - Adds minimal friction (single click)
  
- **Format dependency**: CHANGELOG entries follow Keep a Changelog convention
  - Deviates from typical "What changed" vs. "Why changed" separation
  - Uses semantic versioning approach for release grouping

## Related
- [[auto-labeling]] - PR title patterns used to generate changelog categories (feat, fix, chore)  
- [[adr003-auto-pr-labels]] - Label configuration that defines PR type conventions  
- [[pr-templates]] - PR template requirements that ensure consistency in descriptions  

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
