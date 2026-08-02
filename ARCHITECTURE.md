# RL Vault — Architecture (Milestone 1)

## Stack decisions
- **Kotlin, no Jetpack Compose assumed yet** — kept UI layer untouched per spec (UI not built this milestone).
- **Raw SQLite via `SQLiteOpenHelper`**, not Room. Room needs annotation processing/KSP configured
  in Android Studio easily; since the project must be buildable with Gradle CLI only and stay
  editor-agnostic, raw SQLite + hand-written DAOs avoids any codegen fragility. Schema is a plain
  `.sql` file (`schema.sql`) so it's inspectable/versioned independent of Kotlin code.
- **MVVM**: `data/` (db, model, repository) is UI-agnostic and testable from the JVM without an
  emulator. `ui/<feature>/` folders will each get a ViewModel + Fragment/Compose screen later,
  talking only to `repository/`, never to `db/` directly.
- **No Firebase/cloud/auth** — confirmed nowhere in dependencies; everything resolves locally.

## Folder layout
```
RLVault/
  app/src/main/java/com/rlvault/
    data/
      db/            SQLiteOpenHelper, schema.sql, migrations
      model/         plain Kotlin data classes (Clip, Session, Achievement, Mechanic)
      repository/    single source of truth per domain, used by ViewModels
    ui/
      home/ clips/ review/ sessions/ stats/ achievements/ dev/
    util/            file scanning, folder indexing, achievement rule evaluator
  gradle/            wrapper (CLI-buildable, no Android Studio required)
```

## Schema design notes
- `clips.file_path` is the only pointer to the actual video — RL Vault indexes, never moves/copies,
  per spec. `UNIQUE` prevents duplicate indexing of the same file.
- `rating` is a bare integer (no denominator column) since the spec fixes the denominator at /10
  for display everywhere and explicitly allows >10.
- `mechanics` / `clip_mechanics` is a standard many-to-many junction, since a clip can have
  multiple mechanics and a mechanic spans many clips.
- `sessions.is_automatic` distinguishes the Start/End Session flow from manual entry, but both
  share one table since every session (auto or manual) ends up with the same fields
  (date, duration, wins, losses, rank, notes).
- `clips.session_id` is nullable + `ON DELETE SET NULL` so deleting a session never deletes clips.
- `achievements.condition_json` stores the unlock rule as data, not code — this satisfies the
  "softcoded, editable in Developer Mode" requirement. A rule evaluator in `util/` will read these
  rows and check them against clip/session data; no achievement logic is hardcoded in Kotlin.
- `is_milestone` flag reuses the achievements table for Milestones (e.g. "500 Clips") rather than
  a parallel table, since both are "automatic unlock + text description + condition" — but keeps
  them visually/query-separable via the flag.

## Explicitly NOT done yet (per spec, milestone-gated)
- No UI code.
- No ViewModels/Fragments.
- No folder-picker / file-watcher implementation (util/ folders are empty placeholders).
- No Developer Mode logic.
- No Gradle build files populated with real dependencies yet — folder layout only.
