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

## SESSION LOG — data layer verified + achievement engine + Home + Import/Session/Dev screens

(Follows the CI debugging saga in `GITHUB_CI_TRACKER.md` — data layer compile was confirmed green
in GitHub Actions before any of this was built. See that file and `SESSION_HANDOFF.md` for how CI
got there.)

**Achievement rule evaluator** — `util/AchievementEvaluator.kt`
- `evaluateAll()` loops every non-unlocked `Achievement`, parses `conditionJson` with `org.json`,
  and checks two rule types: `clip_count` (against `ClipRepository.count()`) and `mechanic_count`
  (against `ClipRepository.countWithMechanic()`). Calls `markUnlocked()` on anything newly met and
  returns the list of newly-unlocked achievements.
- Malformed `conditionJson` on one row is caught and skipped rather than aborting the whole pass.
- New rule types = one more `when` branch; no caller changes needed (rules are data, not code).

**Manual DI** — `di/ServiceLocator.kt` + `RLVaultApp.kt`
- Decided against Hilt (would reintroduce annotation-processor setup, same reasoning as skipping
  Room). `ServiceLocator` is a plain object holding one `RLVaultDbHelper` and the four repo/
  evaluator instances, built once via `init(context)`.
- `RLVaultApp : Application()` calls `ServiceLocator.init(this)` in `onCreate()`, registered in
  the manifest as `android:name=".RLVaultApp"`.

**Home screen — wired to real data** — `ui/home/`
- `HomeViewModel` now takes the three repos + evaluator as constructor args (via a manual
  `Factory`, since the default no-arg ViewModel factory won't do). On `refresh()` (called from
  `init` and from `HomeActivity.onResume()`): runs the evaluator first, then loads pending-review
  count (`clipRepository.getUnreviewed().size`), last session (`sessionRepository.getLatest()`,
  formatted date + W/L), and latest unlocked achievement title. All three start at `"—"` and flip
  once the coroutine resolves — empty data reads as "nothing yet," not broken.
- `activity_home.xml` gained three nav buttons: Import Clip, Log Session, Developer Mode.

**Import Clip screen** — `ui/clip/ImportClipActivity` + `ImportClipViewModel`
- SAF picker (`ActivityResultContracts.OpenDocument()`, `video/*`). On pick: takes a persistable
  URI permission (survives past the current process — needed since every future launch reads the
  same `content://` URI back out) and inserts a `Clip` row with `filePath = uri.toString()`,
  `title` from the SAF display name, both timestamps stamped to "now" (no reliable file-creation
  timestamp available via SAF URI yet — real metadata reading is a later pass).

**Log Session screen** — `ui/session/LogSessionActivity` + `LogSessionViewModel`
- Manual entry form: wins/losses/rank/notes → inserts a `Session` with `isAutomatic = false`. This
  is only the manual half of the spec's two session flows — the auto-tracked Start/End Session
  flow (`startAutomaticSession`/`endAutomaticSession`) is still unbuilt.

**Developer Mode screen** — `ui/dev/DeveloperModeActivity` + `DeveloperModeViewModel`
- Two actions only, matching what the spec/trackers had already called out as existing needs:
  **Recalculate Statistics** (re-runs `AchievementEvaluator.evaluateAll()`, reports what unlocked)
  and **Reset Achievement Progress** (behind a confirm dialog, calls
  `AchievementRepository.resetAllProgress()`). Achievement create/edit (`upsert`/`delete`),
  database tools, and "rescan clip folder" from the original Developer Mode spec are NOT built yet.

**CI** — `.github/workflows/build.yml` extended from `compileDebugKotlin` to `assembleDebug` +
`actions/upload-artifact@v4` uploading `app-debug.apk`, now that there's a real launcher Activity
worth packaging.

**Not yet verified on a real device as of this session's end:** the SAF file-picker + persisted
URI permission flow in Import Clip. CI passing only proves it compiles, not that picking a file
and reading `pendingReviewValue` tick up afterward actually works. First real-device smoke test:
open the app, tap Import Clip, pick a video, back out to Home, confirm the pending-review count
increments.

---

## SESSION LOG — missing-files fix + real-device confirmation

- First push of the Home/evaluator pass silently dropped `util/AchievementEvaluator.kt` and the
  `androidx.activity:activity-ktx` dependency in `app/build.gradle.kts` — CI failed with
  `Unresolved reference: util` / `Unresolved reference: viewModels` across every file that used
  them, and CI was still running the OLD `compileDebugKotlin` command, confirming the whole first
  zip's changes never landed, only later passes did.
- Fix: re-delivered all files as one single consolidated zip (rather than three separate ones) to
  remove any chance of a partial apply. Sam confirmed `util/AchievementEvaluator.kt` was in fact
  missing locally before re-applying.
- **Confirmed on a real device via `adb install`:** SAF file picker works, Import Clip successfully
  indexes a video as a `Clip` row, and the Home screen's Pending Review counter correctly
  incremented (+1) after import. First real-device proof this stack actually works end to end, not
  just "compiles in CI."
- **Known gap surfaced by that test:** no way to review an imported clip yet — `markReviewed()`
  exists on `ClipRepository` but nothing in the UI calls it. This is the next thing being built.

---

## SESSION LOG — full Clip Review flow built

**Clip Review screen** — `ui/review/`
- `ClipListActivity` + `ClipListViewModel` + `ClipAdapter`: RecyclerView showing every clip with
  `reviewed = false`, oldest import first (via `ClipRepository.getUnreviewed()`). Reachable from a
  new "Review Clips" button on Home. `onResume()` refreshes the list so a clip just reviewed in
  detail drops out immediately.
- `ClipDetailActivity` + `ClipDetailViewModel`: tapping a clip opens its review form — rating
  (numeric), favorite checkbox, notes, and a comma-separated mechanics tag input (freeform text,
  get-or-create per name via `MechanicRepository.getOrCreate()` so retyping an existing tag reuses
  its row instead of duplicating it). "Play" hands the clip's stored `content://` URI to the
  phone's own video app via `ACTION_VIEW` — RL Vault still never embeds a player or copies the
  file, same as the import philosophy.
- **Mark Reviewed** saves rating/favorite/notes, calls `setMechanics()`, calls `markReviewed()`,
  then re-runs `AchievementEvaluator.evaluateAll()` — this is the one path that can make a
  `mechanic_count` rule fire for the first time, since mechanics only ever get attached during
  review. Newly-unlocked achievement titles surface in the save Toast if any fired.
- Added `androidx.recyclerview:recyclerview:1.3.2` dependency and `mechanicRepository` to
  `ServiceLocator` (previously only had the other three repos + evaluator).

**Not yet verified on a real device as of this session's end:** the whole review flow — tapping
Play actually launching a video app off the stored SAF URI, mechanics tagging actually surviving a
save, and a `mechanic_count` achievement actually unlocking end-to-end via Developer Mode →
Recalculate Statistics. All of this compiles but none of it has been clicked through on the phone
yet.

---

## NEXT SESSION SHOULD PROBABLY START WITH
1. Real-device test of the review flow above — this is the priority, same reasoning as the Import
   Clip real-device test two sessions ago: CI passing only proves it compiles.
2. Once confirmed: try tagging a clip with a mechanic name matching a seeded achievement's
   `condition_json` (e.g. "Flip Reset") enough times to actually watch it unlock — first live proof
   the achievement engine works end-to-end, not just via code read-through.
3. Auto-tracked Start/End Session flow, to complement the manual Log Session screen already built.
4. Achievement definition create/edit in Developer Mode (`upsert`/`delete`) — currently only
   Recalculate/Reset exist there.
5. After every future multi-file delivery: confirm locally (`dir`/`ls` on the new paths) before
   committing — a past session's bug was caused by a partial zip apply going unnoticed until CI ran.
