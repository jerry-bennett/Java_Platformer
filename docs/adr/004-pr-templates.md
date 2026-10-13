# ADR-004: PR Template Standardization

## Status
✅ Approved - Template created and validated

## Context
To improve code review consistency and ensure all necessary information is included with every pull request, we need to enforce a standardized template. Without a clear template, contributors may omit critical details, leading to delayed reviews, incomplete testing verification, and missing documentation updates.

### Problem Statement
- **Incomplete PRs**: Reviewers often have to follow up for missing context or testing instructions
- **Inconsistent communication**: Varying levels of detail make it difficult to assess change impact
- **Documentation gaps**: Changes affecting the game or API may not trigger README/ADR updates
- **Review friction**: Time spent asking clarifying questions instead of reviewing actual code

### Benefits of a PR Template
| Benefit | Impact |
|---------|--------|
| Clear expectations set upfront | Reduces back-and-forth communication |
| Testing requirements explicit | Catches overlooked regression risks |
| Documentation gates enforced | Ensures breaking changes are visible to all |
| Self-review requirement | Improves code quality before review |

## Decision
Create a mandatory PR template with four required sections:

1. **What changed?** - Description of the changes made (high-level summary)
2. **Why?** - Problem statement, requirements, or design decision context explaining the necessity
3. **How to test?** - Step-by-step instructions for verifying the changes work correctly
4. **Checklist** - Required items:
   - [ ] CI tests pass
   - [ ] Self-review completed (code quality, edge cases)
   - [ ] README/ADR documentation updated if applicable

### Implementation Details

The template will be placed at `.github/PULL_REQUEST_TEMPLATE.md` and enforced through:
- GitHub's built-in PR template enforcement
- Optional requirement for contributors to understand the purpose of each section
- Reviewers using checklist as gating criteria (PR may require changes before merging)

## Impact Analysis

### Benefits
- ✅ Reduces reviewer follow-up questions by ~40% (estimated)
- ✅ Standardized communication across all contributions
- ✅ Ensures testing instructions are provided for complex changes
- ✅ Creates explicit gate for documentation updates on relevant changes
- ✅ Encourages contributors to review their own code before submission

### Considerations
- Contributors may initially skip sections thinking they're optional (template will be required)
- Some minor fixes may not need full "Why?" context, but should still include reasoning
- Template must remain concise enough that it doesn't become a documentation burden itself

## Related
- [[auto-labeling]] - PR title pattern matching and auto-labeling workflow  
- [[adr003-auto-pr-labels]] - Automated label assignment based on PR titles  
- [[code-review-guidelines]] - Code review standards and requirements  

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
