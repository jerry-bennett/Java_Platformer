#!/usr/bin/env bash
set -euo pipefail

# Generate CHANGELOG.md from merged PRs in a project
# Requires: gh CLI with appropriate permissions, CHANGELOG.md already exists or will be created

REPO="${GH_REPO:-}"
OUTPUT_FILE="CHANGELOG.md"

if [[ -z "$REPO" ]]; then
  echo "REPO environment variable required for gen-changelog.sh" >&2
  exit 1
fi

# Get all merged PRs in chronological order (oldest first)
CHANGES=$(gh pr list --base main --state merged \
  --limit 0 \
  --json number,title,author,body,url \
  | jq -r '[.[] | {title: .title, body: .body}] | sort_by(.mergedAt) | .[]' \
    2>/dev/null || echo "")

if [[ -z "$CHANGES" ]]; then
  # No PRs to process (might be first run or empty repo)
  # Optionally use a CHANGELOG template as starting point
  if [[ ! -f "$OUTPUT_FILE" ]]; then
    cat > "$OUTPUT_FILE" << 'EOF'
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

- Add initial changelog generation support for Java_Platformer
EOF
  fi
  exit 0
fi

# Create directory structure for the new PR branch
git config --global user.email "41898282+github-actions[bot]@users.noreply.github.com" \
         user.name "github-actions[bot]" || true

# Start a new branch for the changelog PR
BRANCH="changelog/auto-${REPO/\//_-}"
git checkout -b "$BRANCH" 2>/dev/null || git checkout main && git checkout -b "$BRANCH"

# Generate entries for each PR
{
  echo "# Changelog"
  echo ""
  echo "All notable changes to this project will be documented in this file."
  echo ""
  echo "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),"
  echo "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)."
  echo ""

  for pr in $CHANGES; do
    # Parse PR data from JSON
    PR_NUMBER=$(echo "$pr" | jq -r '.number')
    PR_TITLE=$(echo "$pr" | jq -r '.title')
    PR_BODY=$(echo "$pr" | jq -r '.body' | sed 's/"/\\"/g')

    # Extract categories from PR description if present (e.g., [feat], [fix])
    CATEGORY=$(echo "$PR_BODY" | grep -oP '(?<=\[).*(?=,\w|\]|\))' | head -1)

    # Check if this is an initial commit for a feature branch
    INITIAL_COMMIT=$(gh run list --workflow changelog.yml \
        --branch "$BRANCH" --json head_sha,status,state,created_at -q '.[] | select(.status == "completed") | .head_sha' 2>/dev/null | head -1)

    if [[ -n "$CATEGORY" && -z "$INITIAL_COMMIT" ]]; then
      echo "## v$(echo "$pr" | jq -r '.mergedAt' | tr -d '"')"
      echo ""
      echo "* $PR_NUMBER: $PR_TITLE by <cite>$(echo "$pr" | jq -r '.author.login')</cite>"
      echo ""

      # Extract bullet points from the PR body that aren't in changelog format
      # This filters out the [feature|fix|refactor] and other boilerplate text
      echo "$(gh view pr $PR_NUMBER --json title,repository,url | \
        sed -n '2p' | \
        grep -E "^\* " | \
        grep -vP "(?<=\[).*(?=,\w|\]|)" \
        sed 's/^\[\*\]//g')")" 2>/dev/null || true

    elif [[ ! -n "$CATEGORY" ]]; then
      echo "$(gh view pr $PR_NUMBER --json title,repository,url | \
        sed -n '2p' | \
        grep -E "^\* " | \
        sed 's/^\[\*\]//g')")" 2>/dev/null || true
    fi

    echo ""
  done
} > "$OUTPUT_FILE"

# Create the PR to merge back into main
gh pr create --base main --head "$BRANCH" \
  --title "chore: update CHANGELOG.md" \
  --body "Auto-generated changelog update from merged PRs by \`gen-changelog.sh\`.

Please review and merge when ready."

echo "Changelog generated successfully!"
echo "Branch: $BRANCH"
