# Java Platformer

A 2D platformer game built with **libGDX** (migrated from pure Java).

## Development History

| Era | Tech Stack | Notes |
|-----|-----------|-------|
| **Original** | Pure Java, GdxLib | Game ran directly in `run.bat` |
| **Current** | Gradle + libGDX + LWJGL3 | Better tooling, cross-platform support |

> Migration to libGDX improved build management and platform compatibility while keeping core game logic intact.

## Project Structure

| Module | Purpose |
|--------|---------|
| `GDX_Platformer/core` | Shared game logic and gameplay systems |
| `GDX_Platformer/lwjgl3` | Desktop launcher using LWJGL3 |
| `assets/maps/` | LDTK level files for level design |

## Requirements

- Java 11+
- Gradle 8.x

## Quick Start

### Legacy (Pure Java)
The original `run.bat` launches the game as a pure Java application.

### Recommended (libGDX)
Gradle-based build runs via LWJGL3 for better tooling support.

```bash
# Windows
gradlew.bat lwjgl3:run

# Linux/macOS
./gradlew lwjgl3:run
```

> **Note:** Use `./gradlew` on Windows (Unix syntax) to avoid issues with line endings.

## Build Commands

```bash
# Build and run
gradlew lwjgl3:run

# Clean build artifacts
gradlew clean

# Generate IDE files
gradlew idea
```

## Controls

| Key | Action |
|-----|--------|
| `W` / `Space` | Jump |
| `A` | Move Left |
| `D` | Move Right |
| `E` | Interact with NPCs |
| `ESC` | Exit game |

## Level Design

Levels are designed in **LDTK (Lightweight Design Toolkit)** format. Editing levels requires LDTK software or a compatible editor.

