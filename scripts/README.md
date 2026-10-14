# Scripts for Java_Platformer

This directory contains helper scripts used by the CI/CD workflows.

## gen-changelog.sh

Automatically generates `CHANGELOG.md` from merged PRs using the GitHub CLI (`gh`).

### Prerequisites

1. **GitHub CLI installed** (`brew install gh` or `choco install gh`)
2. **PAT secret configured**: In your repository Settings → Secrets and variables → Actions:
   - Create a new repository secret named `CHANGELOG_PAT`
   - The PAT should have: `repo: full_control` scope (or at minimum `contents: write` + `pull_requests: write`)

### How it Works

1. Fetches all merged PRs from the main branch using `gh pr list`
2. Generates changelog entries in Keep a Changelog format
3. Creates a new branch (`changelog/auto`) with the updated CHANGELOG.md
4. Opens an auto-review PR to merge back into main

### Example Output

```bash
# Run this manually to test (replace GH_TOKEN and REPO if needed):
REPO="java-platformer"
export GH_TOKEN=your_personal_access_token_here

./scripts/gen-changelog.sh
```

## Workflow Integration

The corresponding `.github/workflows/changelog.yml` triggers this script:
- On every push to `main` (except when `CHANGELOG.md` is modified)
- Uses the `CHANGELOG_PAT` secret for authentication
- Runs on Ubuntu LTS
