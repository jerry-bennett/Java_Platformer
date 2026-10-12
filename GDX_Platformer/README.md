# Java_Platformer

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Testing

This project uses **JUnit 5** for unit and integration testing. To run tests:

```bash
./gradlew :core:test --info
```

### Test Coverage

| File | Tests | Description |
|------|-------|-------------|
| `EnemyTest.java` | 6 | Enemy class initialization, hurt timer, respawn |
| `CollisionTest.java` | 10 | Stomp detection, push mechanics, AI sensors |
| **Total** | **16** | Comprehensive coverage of collision logic |

Tests verify:
- ✅ Constructor and state management
- ✅ Hurt timer never goes negative
- ✅ Respawn resets position and velocity to zero
- ✅ Stomp detection via feet overlap and falling condition
- ✅ Enemy push physics (left/right direction)
- ✅ Gap sensor and leap sensor calculations for enemy AI

### CI/CD

Automated tests run on every push to `main`, `feat/*`, or `fix/*` branches via GitHub Actions. Failing tests will block the merge.

See: `.github/workflows/ci.yml`

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Directory Structure

```
GDX_Platformer/
├── core/                          # Main Java source code
│   ├── src/main/java/            # Application logic
│   │   └── com/garbageforlust/game/
│   │       ├── Enemy.java        # Enemy class with hitbox, velocity, hurt timer
│   │       ├── GameScreen.java   # Main game loop & collision logic
│   └── src/test/java/            # JUnit 5 tests
│       └── com/garbageforlust/game/
│           ├── EnemyTest.java    # Unit tests for Enemy class
│           └── CollisionTest.java # Integration tests for collision mechanics
├── assets/                        # Game resources (images, audio)
├── docs/                          # Architecture decision records & documentation
├── lwjgl3/                       # Desktop platform build
└── .github/                       # GitHub Actions CI workflows
```

## Documentation

- **[docs/adr/](docs/adr/)** - Architecture Decision Records explaining key design choices
  - `001-enemy-collision.md` - Enemy collision implementation & stomp detection
  - `002-tests-coverage.md` - Test simplification approach for failing tests

## Legacy Code (BlackSquare)

All files from the original BlackSquare Java game project have been archived in `LEGACY/`:

- **Legacy modules**: `LEGACY/java/`, `LEGACY/fonts/`, `LEGACY/sfx/`, `LEGACY/levels/`
- **Reference docs**: `LEGACY/docs/`
- **Old class files**: Compiled `.class` files preserved for reference

## Contributing

When adding new features or bug fixes:

1. Run all tests to ensure they pass (`./gradlew :core:test`)
2. Create a branch for your changes
3. Write tests for new functionality (see test guidelines)
4. Submit a pull request that includes passing tests

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
