# RL Vault — Context Tracker

> Handoff artifact. Paste the FULL PROMPT section below into a new session along with this
> tracker so Claude has complete project context without re-explaining from scratch.

---

## STATUS SUMMARY (as of this session)

**Phase:** Milestone 1 — Architecture, Database, Gradle build config (wrapper now included), and
a fully implemented data layer (models, DB helper, repository interfaces + SQLite implementations).
Still no UI code, no achievement rule evaluator, no folder indexing/SAF.

### What WAS done
- Created project folder layout with MVVM-oriented package split: `data/db`, `data/model`,
  `data/repository`, `ui/<feature>` (home, clips, review, sessions, stats, achievements, dev),
  `util/`.
- Wrote full SQLite schema (`schema.sql`) covering: `clips`, `mechanics`, `clip_mechanics`,
  `sessions`, `achievements`. Achievements table designed to be softcoded via a `condition_json`
  column, doubles as the `Milestones` store via an `is_milestone` flag.
- Wrote `ARCHITECTURE.md` explaining every structural decision.
- Wrote Gradle build config by hand (Kotlin DSL): root `settings.gradle.kts`, root
  `build.gradle.kts` (AGP 8.5.2 + Kotlin 1.9.24 plugins), `app/build.gradle.kts` (namespace
  `com.rlvault`, minSdk 26, target/compileSdk 34, viewBinding on, no Compose, no Firebase/network
  deps — only core-ktx, appcompat, material, lifecycle-viewmodel/livedata-ktx, constraintlayout,
  androidx.sqlite-ktx).
- Added `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.7), minimal
  `AndroidManifest.xml` (no INTERNET permission, no activities yet), placeholder
  `res/values/themes.xml` (Material3 DayNight + purple `#8B5CF6`), empty `proguard-rules.pro`.

### What WAS done (this session, additional)
- Kotlin model data classes: `Clip`, `Mechanic`, `Session`, `Achievement` (all in `data/model/`),
  matching schema.sql field-for-field.
- `RLVaultDbHelper : SQLiteOpenHelper` — reads `res/raw/schema.sql` at `onCreate`, splits on `;`,
  execs each statement (skips PRAGMA lines, since `onConfigure` enables foreign keys per-connection
  instead). `onUpgrade` is a stub with a comment that future migrations must ALTER, not drop.
  Copied `schema.sql` into `res/raw/schema.sql` so it's loadable as an Android raw resource (the
  copy under `data/db/schema.sql` remains the human-readable/source-controlled reference copy —
  **these two files must be kept in sync manually until a build-time copy step exists**).
- `Contract.kt` — table/column name constants (Clips, Mechanics, ClipMechanics, Sessions,
  Achievements objects) so DAOs/repos never hand-type raw SQL identifier strings.
- Repository *interfaces* only (no implementations yet): `ClipRepository`, `SessionRepository`,
  `MechanicRepository`, `AchievementRepository` — all suspend-fun based, in `data/repository/`.

### What WAS done (this session, additional)
- **Gradle wrapper resolved**: Sam supplied `gradlew`, `gradlew.bat`, and `gradle-wrapper.jar`
  (Gradle 8.7) generated outside the sandbox. Merged into the project; `.gradle/` build-cache
  folder from that upload was stripped back out before repackaging (it's local build cache, not
  source — should not be committed).
- `CursorExt.kt` — small null-safe `Cursor` extension functions (`getStringOrNull`, `getLongReq`,
  `getBool`, etc.) shared across repository implementations to avoid boilerplate.
- **Repository implementations, all backed by `RLVaultDbHelper` / raw `SQLiteDatabase` calls,
  all hopping to `Dispatchers.IO`:**
  - `SqliteClipRepository` — full CRUD, `getUnreviewed()`, `getBySession()`, `count()`,
    `countWithMechanic()` (for achievement rules like "100 clips tagged Flip Reset"), and
    clip↔mechanic junction-table sync via `setMechanics()`.
  - `SqliteMechanicRepository` — `getOrCreate()` for freeform tag entry during review, so typing
    an existing mechanic name doesn't create a duplicate row.
  - `SqliteSessionRepository` — full CRUD plus `startAutomaticSession()` / `endAutomaticSession()`
    to back the Start/End Session flow (computes `duration_ms` from stored `start_time`).
  - `SqliteAchievementRepository` — full CRUD (`upsert` for Developer Mode create/edit),
    `getUnlocked()`, `getMilestones()` (via `is_milestone` flag), `markUnlocked()`,
    `resetAllProgress()` (Developer Mode "Reset achievement progress").
- Added `kotlinx-coroutines-android:1.8.1` dependency to `app/build.gradle.kts` to support the
  `suspend`/`Dispatchers.IO` pattern used throughout the repo implementations.
- Cross-checked every `Contract.kt` constant referenced by the new repo files — all resolve
  correctly (no typos/missing constants).

### What is NOT done yet
- No UI screens — explicitly deferred per spec.
- No folder-picker / SAF integration, no file-watcher/rescan logic.
- No achievement rule evaluator (the piece that reads `condition_json`, calls
  `countWithMechanic()`/`count()`, and invokes `markUnlocked()` — repositories are ready for it,
  logic itself doesn't exist yet).
- No Developer Mode tap-7-times unlock flow / dev panel UI.
- No migrations strategy beyond the standalone `.sql` file (still fine — DB is at version 1).
- No dependency-injection wiring (something needs to construct `RLVaultDbHelper` once and hand
  the four repositories to ViewModels — currently each repo just takes a `RLVaultDbHelper` in its
  constructor with no DI framework chosen yet).
- Build has still NOT been run/compiled in this environment (no Gradle binary available in the
  sandbox itself, only the wrapper artifacts) — recommend Sam run `./gradlew compileDebugKotlin`
  locally as a sanity check before continuing.

### Files created / modified this session
- `RLVault/ARCHITECTURE.md`, `RLVault/CONTEXT_TRACKER.md` (updated)
- `RLVault/app/src/main/java/com/rlvault/data/db/schema.sql`
- `RLVault/settings.gradle.kts`, `RLVault/build.gradle.kts`, `RLVault/app/build.gradle.kts`
- `RLVault/gradle.properties`, `RLVault/gradle/wrapper/gradle-wrapper.properties`
- `RLVault/app/src/main/AndroidManifest.xml`
- `RLVault/app/src/main/res/values/themes.xml` (placeholder)
- `RLVault/app/proguard-rules.pro` (empty stub)
- `RLVault/app/src/main/java/com/rlvault/data/model/{Clip,Mechanic,Session,Achievement}.kt`
- `RLVault/app/src/main/java/com/rlvault/data/db/RLVaultDbHelper.kt`
- `RLVault/app/src/main/java/com/rlvault/data/db/Contract.kt`
- `RLVault/app/src/main/java/com/rlvault/data/db/CursorExt.kt` (new)
- `RLVault/app/src/main/res/raw/schema.sql` (resource copy of the schema)
- `RLVault/app/src/main/java/com/rlvault/data/repository/{ClipRepository,SessionRepository,
  MechanicRepository,AchievementRepository}.kt` (interfaces)
- `RLVault/app/src/main/java/com/rlvault/data/repository/Sqlite{Clip,Session,Mechanic,
  Achievement}Repository.kt` (new — implementations)
- `RLVault/app/build.gradle.kts` (modified — added kotlinx-coroutines-android dependency)
- `RLVault/gradlew`, `RLVault/gradlew.bat`, `RLVault/gradle/wrapper/gradle-wrapper.jar`
  (provided by Sam this session, not generated by Claude — sandbox has no Gradle/network access
  to fetch these)
- Empty package folders under `app/src/main/java/com/rlvault/` (ui/*, util) — no Kotlin files yet.

### Requirement to preserve across sessions
**Always generate/update this context tracker file at the end of a work session**, covering: what
was done, what is still outstanding, and the exact list of files created or modified — so the next
session can resume without re-deriving project state. Keep the full original prompt (below)
attached so intent/constraints are never lost or re-interpreted.

---

## FULL ORIGINAL PROMPT (verbatim, for future sessions)

We are starting a brand-new Android application called RL Vault.

IMPORTANT:
Before writing code, read the project specification below carefully and follow it exactly.

====================================================
PROJECT GOAL
====================================================

RL Vault is a completely offline Android application.

Its purpose is to archive a player's Rocket League journey.

It is NOT:
- a replay parser
- a montage editor
- a social platform
- a coaching app

It is simply a personal career archive.

Everything is stored locally.

====================================================
TECH STACK
====================================================

Language:
Kotlin

Database:
SQLite

Architecture:
MVVM (preferred)

No Firebase.
No cloud.
No authentication.
No internet required.

Target:
Android phone.

Do NOT assume Android Studio is available.
Keep the project portable and editor-agnostic.
The project should be buildable and testable using Gradle and Android SDK command-line tools only.

====================================================
VISUAL STYLE
====================================================

Dark AMOLED (with optional Light Mode).

Primary accent:
Purple (#8B5CF6)

Modern.
Minimal.
Fast.
Material 3 inspired.

====================================================
CORE FEATURES
====================================================

HOME

Shows:

- Last session summary
- Pending review count
- Statistics summary
- Latest unlocked achievement
- Current goal/progress

No information overload.

----------------------------------------------------

CLIPS

User selects ONE folder on first launch.

Example:

Movies/
    RL Clips/

RL Vault NEVER moves or copies files.

It only indexes them.

Stored metadata includes:

- absolute file path
- title
- notes
- rating
- mechanics
- favorite
- created date
- duration

----------------------------------------------------

IMPORT

When new clips appear inside the selected folder:

"12 new clips found."

User can review now or later.

----------------------------------------------------

REVIEW QUEUE

Imported clips enter a review queue.

Flow:

Clip 1
↓

Save
↓

Clip 2
↓

Save
...

Each review contains:

- Title
- Rating
- Mechanics
- Notes
- Favorite toggle

Buttons:

- Previous
- Next
- Skip
- Save

----------------------------------------------------

RATING SYSTEM

Ratings are NOT stars.

Ratings are stored as integer values.

Displayed format:

7/10

10/10

11/10

21/10

The denominator is ALWAYS 10.

Ratings above 10 are allowed.

----------------------------------------------------

MECHANICS

Each clip may contain MULTIPLE mechanics.

Examples:

- Flip Reset
- Musty
- Pinch
- Air Dribble
- Double Tap

etc.

----------------------------------------------------

SESSIONS

Support BOTH:

1.

Automatic

Start Session
↓

During the session automatically track:
- Start time
- End time
- Total duration
- Number of clips imported during that session

When ending the session, prompt the user to enter:

- Wins
- Losses
- Rank (optional)
- Notes

OR

2.

Manual creation

User can manually enter:

- Date
- Duration
- Wins
- Losses
- Rank (optional)
- Notes

----------------------------------------------------

PROGRESS

Automatically generated from:

- Clips
- Sessions
- Mechanics

No fake percentage bars.

Display actual statistics and historical progression.

----------------------------------------------------

STATISTICS

Dedicated page.

Homepage only shows a summary.

----------------------------------------------------

ACHIEVEMENTS

Automatic unlocks.

NO icons.

Text only.

Softcoded.

Achievement definitions must NOT be hardcoded into Kotlin classes.
They should be editable later through Developer Mode.

Unlocks are calculated automatically based entirely on clip metadata.

Example:
If 100 clips contain the "Flip Reset" mechanic tag,
the corresponding achievement unlocks automatically.

----------------------------------------------------

MILESTONES

Separate from achievements.

Examples:

- 500 Clips
- 1000 Clips
- etc.

----------------------------------------------------

DEVELOPER MODE

Unlock by tapping:

Settings
↓

About
↓

Version

7 times.

Developer Panel allows:

- Achievement editing / creation
- Reset achievement progress
- Database tools
- Recalculate statistics
- Rescan clip folder (if file monitoring misses newly added clips)

====================================================
DATABASE
====================================================

Use SQLite.

Initial tables should include:

- clips
- mechanics
- clip_mechanics
- sessions
- achievements

Design them properly with future scalability in mind.

====================================================
IMPORTANT
====================================================

Do NOT implement every feature immediately.

We are building RL Vault in milestones.

Start ONLY with:

1. Project architecture.
2. SQLite database structure.
3. Folder layout.
4. Explain WHY each architectural decision was made.
5. Do NOT build the UI yet.
6. Avoid placeholder code unless absolutely necessary.

Act like a senior Android engineer preparing a production-quality project.

---

## NEXT SESSION SHOULD PROBABLY START WITH
1. Run `./gradlew compileDebugKotlin` (or full `assembleDebug`) locally to confirm the data layer
   actually compiles — it has NOT been compiled/verified anywhere yet, only hand-written.
2. Write the achievement rule evaluator in `util/` that reads `condition_json` off
   `AchievementRepository.getAll()`, checks rules against `ClipRepository.count()` /
   `countWithMechanic()` / session data, and calls `markUnlocked()` when a threshold is crossed.
   Start with two rule types: `mechanic_count` and `clip_count`.
3. Decide on a DI approach (manual singleton container is simplest given no framework chosen —
   Hilt would reintroduce annotation-processing setup, which conflicts with the "no Android
   Studio assumed" constraint unless verified to work headless).
4. Only after the data layer is verified compiling: start Home screen ViewModel + layout.
