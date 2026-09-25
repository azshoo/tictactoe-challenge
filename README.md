# TicTacToe App: end-to-end tests

A few end-to-end UI tests for the TicTacToe Android app (`com.qatest.xo_qa_challenge`). The scenarios cover the core
game mechanics — winning on every line, the result banner, the score, occupied cells, the end of the game — and were
chosen to catch the most critical bugs found during exploratory testing.

**The tests are expected to be red.** Every scenario targets a bug that is present in the current build, so every test
fails, each on its own bug:

| Test | Bugs it catches |
|---|---|
| `WinCombinationsTest` (16 cases: 8 lines × X/O) | wrong name in the win banner, 2 points per win, win line drawn through the wrong row, a win on the [1, 3]–[2, 2]–[3, 1] diagonal not detected |
| `NinthMoveWinTest` | a win on the 9th move declared a draw |
| `OccupiedCellTest` | an occupied cell can be overwritten |
| `GameStopsAfterWinTest` | the board accepts moves after the game ends |

## Tools and architecture

### Tools

The app is a production build: no sources, no test hooks, no debug options. The tests are therefore black-box: they
see only what the device shows and what the accessibility tree exposes.

They are written in Kotlin with UiAutomator — the native Android tool, run as ordinary instrumented tests by Gradle,
with no extra server or driver layer (unlike Appium or Astur). Any other generic mobile automation tool would not
change the basic approach much.

The app is built with Flutter. For actual dev builds, Flutter's own tools (`integration_test`, Patrol) would be a more
natural choice, but again, that is optional.

### Architecture

- **Tests** (`tests/`) are black-box scenarios in Arrange / Act / Assert form. They talk to the app only through page
  objects.
- **Page objects** (`pageobjects/`) implement interaction with the screen:
  - `Board` — the game board: make a move, play a sequence of moves, read a cell, check the win line;
  - `GameScreen` — everything else on the screen: launching the app, the result banner, the score, player names,
    Reset and Clear buttons.

  The board and the rest of the screen are split for easier navigation. In a larger app `GameScreen` would be split
  further into smaller parts (score card, names, controls).
- **Page object methods are stateless by design.** The tests do not control the app's state, so a page object that
  remembered it (a cached score, a remembered turn) would drift from the real screen. Every method reads the screen
  again. The only state is the board map built by `CellLocator` (see below); it is rebuilt at every app launch.
- **Locators are not moved to a separate catalogue.** With proper test ids in the accessibility attributes they would
  belong in a separate catalogue file, for reuse and easier maintenance.
- **Test data** (`testdata/`) — the 16 winning move sequences for `WinCombinationsTest`, in JSON.
- **Configuration** (`assets/`) — `config.yml` (app package, board size, timeouts, orientation) and `vision.yml`
  (mark and line colours).

```
app/src/androidTest/
  java/
    tests/          the test scenarios
    testdata/       WinCases.kt, win_cases.json
    pageobjects/    Board, GameScreen
    util/           CellLocator, CellVision, config, device helpers, failure artifacts
  assets/           config.yml, vision.yml
```

### Implementation details

**Cell state is read from screenshots.** The app keeps the board's content out of the accessibility tree: a cell is an
unlabeled button, and an X, an O or the win line are not visible to UiAutomator at all. The tests therefore read them
from a screenshot: `CellVision` crops the cell and counts pixels of the X, O and line colours with OpenCV. This is not
the optimal solution, but it is direct and was verified against the app before the tests were written: 2790 cells on
310 screenshots classified without a single error.

**Board calibration.** The cells carry no ids, but each cell's accessibility node keeps its identity through scrolling,
rotation, moves and resets. After every app launch `CellLocator` maps the nodes to `[row, col]`, scrolling down only if the
board does not fit on the screen. After that, any cell is found by its node wherever the screen is scrolled, and is scrolled fully onto the
screen before a move or a check.

**Soft assertions.** A win case checks several things, and every one of them should be reported. The usual tool,
AssertJ's soft assertions, does not work on Android. The tests use Truth's `Expect`, which collects failures on Android,
with a small handler of their own, `assert(condition, message)` in `util/SoftAssert.kt`, so that a failed check is
reported as its message only.

**Rotation** is done by the test itself, after the app is on screen: on Android 15+ emulators a rotation set while
the launcher is on top is reverted when the app starts.

## Limitations and known issues

- **Not tested on real devices.** The tests were developed on an arm64 Android 17 (API 37) emulator; CI runs them on
  an x86_64 one.
- **Developed and run on macOS.** Not tested on Windows.
- **Not optimal in speed.** Finding cells means walking the accessibility tree, and reading them means taking
  screenshots. A test takes 2–8 s, the portrait run about 2 minutes on an emulator.
- **Landscape is slow**: about 7 minutes. The board does not fit the screen in landscape, and every standard
  UiAutomator scroll step takes about 1.3 s: it waits up to 1 s for the scroll to finish, then 250 ms more.
- **The colours are measured on this build.** A change of the app's theme or palette needs `vision.yml` measured
  again.
- **Cells are recognised as the only unlabeled buttons on the screen.** Another unlabeled button would break this.
- **Timing-based waits.** Timeouts in `config.yml` are set from measurements on an emulator; a much slower device may
  need them raised.

## Setup and run

### Setup

```bash
./setup.sh
```

It installs what is missing and leaves the rest as is: a JDK (with SDKMAN), the Android SDK in the standard location,
and an emulator named `tictactoe`. Start the emulator from Android Studio's Device Manager, or with the command
`setup.sh` prints at the end.

### Run

```bash
./gradlew connectedDebugAndroidTest -Paut=/absolute/path/to/app.apk
```

`-Paut` installs the app under test before the tests run; without it the app must already be on the device. A relative
path is resolved from the repository root. `~` is not expanded inside `-Paut=~/…` by bash or zsh; use `$HOME` instead.

Options:

| What | How |
|---|---|
| Pick a device when several are connected | `ANDROID_SERIAL=emulator-5554 ./gradlew …` (serials: `adb devices`) |
| Landscape | `-Pandroid.testInstrumentationRunnerArguments.orientation=landscape` (default: `orientation` in `config.yml`) |
| One test class | `-Pandroid.testInstrumentationRunnerArguments.class=com.qa.tictactoe.tests.OccupiedCellTest` |

### CI

`.github/workflows/tests.yml` runs the tests on push on an Android 17 (API 37) Pixel 7 emulator. It downloads the app under test
from the URL in the `AUT_APK_URL` repository secret.

Since every test targets a known bug, the job runs Gradle with `-PignoreTestFailures`. Failed tests do not fail the
build: the run shows them in the red **Test results** check and in the report. Broken tests and any infrastructure
problems fail the build. The reports, failure artifacts and the system events log are uploaded as the `test-reports`
artifact.

## Output

Gradle prints where the report is:

```
> Task :app:connectedDebugAndroidTest
Running tests on devices: QA_Additional(AVD) - 17

> Task :app:connectedDebugAndroidTest FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:connectedDebugAndroidTest'.
> There were failing tests. See the report at: file:///…/app/build/reports/androidTests/connected/debug/index.html

BUILD FAILED in 2m 9s
```

| Output | Where |
|---|---|
| HTML report | `app/build/reports/androidTests/connected/debug/index.html` |
| JUnit XML | `app/build/outputs/androidTest-results/connected/debug/` |
| Screenshot and UI tree of every failed test | `app/build/outputs/connected_android_test_additional_output/` |

Failure message of `X wins on row 2`:

```
3 expectations failed:
  1. Expected the win line to cross row 2, but it was not found there.
  2. Expected the result banner to name Player X, got "Player O Wins!" instead.
  3. Expected Player X to get 1 point, got 2 instead.
```
