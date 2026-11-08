# Agents Guide — Java_Platformer

## Project Overview

**Java_Platformer** is a 2D platformer game built with **libGDX**. It was migrated from a pure Java project (BlackSquare) to use modern tooling: Gradle, libGDX core for shared logic, and LWJGL3 for the desktop launcher.

### Architecture

```
Java_Platformer/
├── GDX_Platformer/                    # Main project (migrated from BlackSquare)
│   ├── core/                          # Shared game logic & gameplay systems
│   │   ├── src/main/java/             # Application logic
│   │   │   └── com/garbageforlust/game/
│   │   │       ├── Enemy.java          # Enemy class with hitbox, velocity, hurt timer
│   │   │       ├── GameScreen.java     # Main game loop & collision logic
│   │   │       ├── Player.java         # Player physics & controls
│   │   │       ├── Level.java          # Level management (LDTK)
│   │   │       └── Dust.java           # Particle effects
│   │   ├── src/test/java/              # JUnit 5 unit/integration tests
│   │   │   └── com/garbageforlust/game/
│   │   │       ├── EnemyTest.java      # Unit tests for Enemy class (6 tests)
│   │   │       └── CollisionTest.java  # Integration tests for collision mechanics (10 tests)
│   ├── assets/                         # Game resources (images, audio)
│   ├── lwjgl3/                        # Desktop platform build
│   └── docs/                          # ADRs & architecture docs
├── LEGACY/                            # Archived BlackSquare project files
├── .github/                           # CI workflows & PR templates
└── scripts/                          # Automation scripts (changelog)
```

---

## Build System: Gradle Multi-Project

**Java_Platformer** uses **Gradle 8.5** with a multi-project setup (`build.gradle` in root manages two subprojects).

### Key Directories & Files

| File | Purpose |
|------|---------|
| `GDX_Platformer/build.gradle` | Root build file: manages both projects, asset list generation |
| `GDX_Platformer/core/build.gradle` | Core module: libGDX dependencies + JUnit 5 tests |
| `GDX_Platformer/lwjgl3/build.gradle` | Desktop launcher: LWJGL3 application jar |
| `GDX_Platformer/settings.gradle` | Declares projects: `:core`, `:lwjgl3` |
| `GDX_Platformer/gradlew` | Gradle wrapper script (Unix syntax) |

### Common Gradle Commands

```bash
# Build and run the desktop application
./gradlew lwjgl3:run

# Run unit tests in core module (JUnit 5)
./gradlew :core:test

# Run all tests with details
./gradlew :core:test --info

# Build runnable JAR for core
./gradlew :lwjgl3:jar

# Clean build artifacts
./gradlew clean
```

---

## Testing Strategy (ADR-002)

The project uses **JUnit 5** configured via Gradle's `useJUnitPlatform()`. Tests live in `core/src/test/java/` and are run via the CI pipeline.

### Test Files & Coverage

| File | Type | Tests | Purpose |
|------|------|-------|---------|
| `EnemyTest.java` | Unit | 6 | Enemy state management, hurt timer, respawn |
| `CollisionTest.java` | Integration | 10 | Stomp detection, push mechanics, AI sensors |
| **Total** | — | **16** | Comprehensive collision logic coverage |

### How to Write Tests

- Use `org.junit.jupiter.api.Test` annotations
- Assert with `Assertions.assertThat()` or direct assertions (`assertEquals()`)
- Place tests in `com.garbageforlust.game.*` package (matching source structure)
- Follow ADR-002 requirements:
  - ✅ Unit tests for each core class
  - ✅ Integration tests for collision mechanics
  - ✅ CI/CD auto-runs on push to `main`, `feat/*`, or `fix/*`

### Running Tests Manually

```bash
cd GDX_Platformer
./gradlew :core:test --tests "*Test"  # All test classes
./gradlew :core:test --info           # With detailed output
```

---

## Code Structure: libGDX Core Module

The `core` module contains the main game logic. Key components:

### `Enemy.java`
- **Location**: `core/src/main/java/com/garbageforlust/game/Enemy.java`
- **Responsibility**: Enemy entity with hitbox (32x32 pixels), velocity, hurt timer, respawn logic
- **State fields**: `bounds`, `velocity`, `hurtTimer`, `wasFalling`
- **Key methods**: constructor, `update()`, `tickHurtTimer()`

### `GameScreen.java`
- **Location**: `core/src/main/java/com/garbageforlust/game/GameScreen.java`
- **Responsibility**: Main game loop, collision detection (player-enemy), AI behavior
- **Key logic** (ADR-001):
  - Stomp detection: player feet overlap enemy top + downward velocity
  - Push mechanic: kicks enemy left/right based on player position
  - Kill condition: stomp OR player above enemy (head-bump)
  - Visual feedback: dust particles on kill

### `Player.java`
- **Location**: `core/src/main/java/com/garbageforlust/game/Player.java`
- **Responsibility**: Player physics, input handling (A/D for movement, W/Space jump)

---

## CI/CD Pipeline (.github/workflows/)

### Main Workflow: CI - Run Tests (`ci.yml`)

Triggers on:
- Push to `main`, `feat/*` branches
- Pull requests targeting `main`

Runs:
```bash
cd GDX_Platformer
./gradlew :core:test --tests "*Test"
```

### PR Labeling Workflow (`pr-labels.yml`)

Automatically assigns labels based on PR title patterns (feat, fix, chore, test, docs):

| Pattern | Example Labels |
|---------|----------------|
| `feat(...)` | `feat` |
| `fix(...)` | `fix` |
| `chore(...)` | `chore` |
| `test(...)` | `test` |
| `docs(...)` | `docs` |
| No pattern + no existing label | `needs-label` |

### Changelog Automation (`changelog.yml`)

- Triggers on every push to `main` (excludes `CHANGELOG.md` changes)
- Runs `scripts/gen-changelog.sh` to extract PR details
- Creates/updates a `changelog/auto` branch with the new CHANGELOG.md
- Opens a PR for human review and manual merge via `gate` check

---

## Documentation Structure (docs/adr/)

### ADRs (Architecture Decision Records)

| File | Topic | Status |
|------|-------|--------|
| `001-enemy-collision.md` | Stomp detection logic, hitbox changes | ✅ Fixed |
| `002-tests-coverage.md` | JUnit 5 setup, test organization | ✅ Implemented |
| `003-auto-pr-labels.md` | Automated PR labeling from title patterns | — |
| `004-pr-templates.md` | PR template structure & requirements | — |
| `005-changelog-automation.md` | CHANGELOG generation via GitHub Actions | — |

### PR Template (`.github/PULL_REQUEST_TEMPLATE.md`)

Required sections for all PRs:

1. **What changed?** — Brief description of changes
2. **Why?** — Problem statement, requirements, or design context
3. **How to test?** — Step-by-step verification instructions
4. **Checklist** — CI passes, self-review, README/ADR updates

### Commit Message Guidelines

- Use conventional commits: `feat(...)` for features, `fix(...)` for bug fixes

## Legacy Project (BlackSquare) — `LEGACY/`

The original BlackSquare project has been **archived** in the `LEGACY/` directory for reference:

| Directory | Contents |
|-----------|----------|
| `LEGACY/java/` | Old Java source files |
| `LEGACY/fonts/` | Legacy font assets |
| `LEGACY/sfx/` | Sound effects |
| `LEGACY/levels/` | LDTK level files |
| `LEGACY/docs/` | Historical documentation |

**Never modify** files in `LEGACY/`. They are read-only reference only.

---

## IDE Setup & File Exclusions

### .gitignore Key Rules

- Exclude `*.class` files (except `LEGACY/**/.class`)
- Exclude `build/`, `.gradle/`, `.idea/` directories
- Exclude VSCode settings: `.vscode/settings.json`

### Recommended IDE Plugins

| IDE | Command | Effect |
|-----|---------|--------|
| IntelliJ IDEA | `gradlew idea` | Generates project structure |
| Eclipse | `gradlew eclipse` | Creates Eclipse project files |

---

## Common Development Tasks

### Before Making Changes

1. **Run all tests** to establish baseline
   ```bash
   ./gradlew :core:test
   ```

2. **Review relevant ADRs** in `docs/adr/` for design context

3. **Check PR template** requirements for documentation updates

### For Adding New Features

1. Create branch: `git checkout -b feat(feature-name)`
2. Write implementation + corresponding tests
3. Run tests locally: `./gradlew :core:test`
4. Commit with conventional commit message
5. Submit PR (labels will be auto-assigned)

### For Bug Fixes

1. Reproduce issue locally
2. Add regression test if possible
3. Use `fix(...)` prefix in commit/PR title
4. Verify all existing tests still pass

---

## Quick Reference: File Paths

```
GDX_Platformer/
├── GDX_Platformer/core/src/main/java/com/garbageforlust/game/
│   ├── Enemy.java                    # Enemy class
│   ├── GameScreen.java               # Main game logic
│   └── Player.java                   # Player class
├── GDX_Platformer/core/src/test/java/com/garbageforlust/game/
│   ├── EnemyTest.java                # Unit tests (6)
│   └── CollisionTest.java            # Integration tests (10)
└── docs/adr/                         # ADR files for design decisions
```

---

## Agents: What You're Reviewing Before Changes

### This file (`agents.md`)

Before modifying any code in `GDX_Platformer/`, an agent should review this guide to understand:

1. **Project structure** — How the multi-project Gradle build is organized
2. **Testing requirements** — JUnit 5 setup, where tests live, what's covered
3. **Build commands** — Which Gradle tasks to run, how to invoke them
4. **Key files** — `Enemy.java`, `GameScreen.java` for collision logic changes
5. **CI/CD expectations** — Test passing is required before merge
6. **Documentation updates** — ADRs, README, PR templates for context

### When reviewing code changes:

- ✅ Verify no tests are skipped or broken unless justified (ADR-002)
- ✅ Ensure collision logic follows ADR-001 (stomp detection = feet + velocity check)
- ✅ Confirm all Gradle modules are properly included in the build
- ✅ Check that legacy code (`LEGACY/`) is untouched (read-only reference)

---

## ⚠️ CRITICAL RULE: Do Not Commit Until Authorized

**Do not commit, push, or modify any tracked files until explicitly instructed by the user.** This includes:

- **Never create commits** unless you have explicit permission from the user
- **Never open pull requests** without being asked
- **Never make changes** to working tree files before confirmation
- Review .github/PULL_REQUEST_TEMPLATE.md before drafting a PR.

If you need to experiment with code:
1. Work in a temporary directory or use print statements/debug logs
2. Test and verify locally first
3. Get explicit approval BEFORE creating any commits or pushing

This rule applies even if the task explicitly says "commit changes" — that was an error in the prompt, not authorization from the user. You must wait for confirmation to commit anything to the repository.

**Commit Message Guidelines** (for future reference)

- Use conventional commits: `feat(...)` for features, `fix(...)` for bug fixes

## Related Memory References

- `[[adr002-test-simplification]]` — User's note on simplifying failing movement tests

---

**Last updated**: 2026-11-07  
**Git branch**: `feat/changelog-automation` (working from: `main`)
