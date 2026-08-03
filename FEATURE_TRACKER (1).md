# RL Vault — Feature/Spec Status Tracker

> Fourth tracker file. Separate from `CONTEXT_TRACKER.md` (code/architecture history, has the full
> original spec pasted in verbatim), `GITHUB_CI_TRACKER.md` (GitHub/CI setup saga), and
> `SESSION_HANDOFF.md` (short "where things stand" note from the pre-UI milestone). This one is a
> running checklist: every feature the spec asked for, whether it's built, and what's still open.
> Read this one FIRST for "what's left" — read `CONTEXT_TRACKER.md` for HOW things were built.

---

## WHAT'S BUILT (all confirmed working on a real device except where noted)

**Data layer** (Milestone 1, verified compiling in CI, confirmed working via the app itself)
- SQLite schema: `clips`, `mechanics`, `clip_mechanics`, `sessions`, `achievements`.
- Full repository interfaces + SQLite implementations for all four.
- `AchievementEvaluator` (`util/`) — reads `condition_json`, checks `clip_count` and
  `mechanic_count` rule types against live data, calls `markUnlocked()`. Confirmed re-run on every
  clip save (review) and every Developer Mode "Recalculate Statistics" tap.
- Manual DI: `ServiceLocator` object + `RLVaultApp : Application()`.

**Home screen** — `ui/home/HomeActivity` + `HomeViewModel`
- Shows: Pending Review count, Last Session (date + W/L), Latest Achievement title.
- Nav buttons: Import Clip, Log Session, Developer Mode, Review Clips, Reviewed Clips.
- **NOT yet shown on Home per spec:** a general "Statistics summary" section, and "Current
  goal/progress" — see gaps below.

**Clip import** — `ui/clip/ImportClipActivity` + `ImportClipViewModel` + `util/ClipFolderPrefs`
- **UPDATED — folder-based import now built.** First launch (or "Change Folder") opens
  `ACTION_OPEN_DOCUMENT_TREE`; the chosen tree URI is persisted (`ClipFolderPrefs`, plain
  SharedPreferences, not a DB table) with a persistable read/write grant. Every subsequent visit
  (and every `onResume()`, so backgrounding the app and adding files still picks them up) re-scans
  the saved folder recursively, diffs found video files against `file_path` already in the DB, and
  inserts only the delta, reporting `"N new clip(s) found."` per spec. Video detection uses MIME
  type with an extension-sniffing fallback (`.mp4/.mkv/.mov/.webm/.avi/.3gp/.m4v`) for providers
  that report a null/generic MIME on tree children. Manual "Rescan" and "Change Folder" buttons
  also present. A manual single-file picker (`ACTION_OPEN_DOCUMENT`) is kept underneath for
  one-off adds outside the selected folder — not a spec gap, just an extra affordance.
- **Not yet device-confirmed** (built and compiling per code, but no note here saying Sam tested
  the folder flow live on a device the way single-file import was) — worth a quick device pass
  before checking this off entirely.

**Clip review** — `ui/review/ClipListActivity` (unreviewed list) + `ClipDetailActivity`
(rating/favorite/notes/mechanics form) + `ReviewedClipListActivity` (everything already reviewed,
tap to re-edit). **Confirmed working on device** for the pre-existing single-clip-at-a-time flow.
- Mechanics entered as freeform comma-separated tags, get-or-create per name.
- Saving re-runs the achievement evaluator every time (so mechanic-tagging can unlock achievements
  retroactively, including on re-edits from the Reviewed list).
- **UPDATED — sequential review queue now built.** `ClipListActivity` launches
  `ClipDetailActivity` with `EXTRA_QUEUE_IDS` (the full unreviewed id list, snapshotted once at
  queue-entry so it doesn't reshuffle under the user as clips drop out mid-review) and
  `EXTRA_QUEUE_POSITION`. `ClipDetailActivity`/`ClipDetailViewModel` branch on whether queue extras
  are present: queue mode shows a Previous/Skip/Next nav row + "Clip X of N" progress line, Save
  persists-then-auto-advances instead of finishing, Skip/Next advance without saving, Previous
  steps back (no-op at position 0, discards unsaved edits with a toast), and walking off either
  end finishes back to `ClipListActivity` with a "queue complete" toast. Single-edit mode (from
  Reviewed list) is untouched — no queue extras, Save finishes immediately, no nav row.
- **Not yet device-confirmed** — same caveat as folder import, this is a code-level confirmation
  from reading `ClipDetailActivity.kt`/`ClipDetailViewModel.kt`, not a "Sam tapped through it on a
  phone" confirmation like the original review screens have.
- **Gap vs. spec — still open:** ratings are stored/shown as a raw integer with no `/10` suffix
  anywhere in the UI (`ClipDetailActivity` just does `clip.rating?.toString()`). Spec is explicit:
  always displayed as `X/10` (e.g. `7/10`, `21/10` — denominator always 10, values above 10
  allowed). Not touched by the queue work.

**Internal video player** — `ui/player/PlayerActivity` (`VideoView` + `MediaController`).
Confirmed launching from Clip Detail's Play button; plays the clip's stored `content://` URI
in-app instead of handing off to another app. Full-device playback confirmed by Sam.

**Session logging** — `ui/session/LogSessionActivity` + `LogSessionViewModel`
- Manual entry only: wins/losses/rank/notes → inserts a `Session` with `isAutomatic = false`.
- **Gap vs. spec:** spec wants BOTH manual (✅ done) and automatic (Start Session → auto-tracks
  start time/end time/duration/clip-count-during-session → prompts for W/L/rank/notes on End).
  Automatic flow is entirely unbuilt; `startAutomaticSession()`/`endAutomaticSession()` exist on
  `SessionRepository` but nothing in the UI calls them.

**Developer Mode** — `ui/dev/DeveloperModeActivity` + `DeveloperModeViewModel`
- Two actions only: Recalculate Statistics, Reset Achievement Progress (confirm dialog).
- **Gap vs. spec:** spec wants the panel gated behind "tap version 7 times" in Settings → About —
  currently it's just a plain nav button on Home, reachable with one tap, no gesture gate.
- **Gap vs. spec:** achievement definition create/edit (`upsert`/`delete` on
  `AchievementRepository`) has no UI yet — only Recalculate/Reset are wired up. Spec also mentions
  "Database tools" and "Rescan clip folder" as Developer Mode features — neither exists.

**CI** — GitHub Actions builds `assembleDebug` and uploads the APK as an artifact on every push.
Confirmed working after one missing-files incident (see below).

---

## KNOWN BUG HISTORY (context for future debugging, don't need to re-litigate)
- A push once silently dropped `util/AchievementEvaluator.kt` and an `activity-ktx` Gradle
  dependency — CI failed with `Unresolved reference` errors across half the codebase. Root cause:
  partial zip apply going unnoticed before commit. Fixed by re-delivering everything as one
  consolidated zip and having Sam confirm the missing folder existed locally before re-applying.
  **Lesson banked:** always spot-check new folders/files exist locally before `git add`.

---

## FULL LIST OF SPEC GAPS (not yet built), roughly in priority order

~~1. Folder-based clip detection~~ — **DONE**, see Clip import section above. Recommend a quick
   on-device pass to move it from "confirmed by code" to "confirmed by Sam," but functionally
   complete.

~~2. Sequential Review Queue~~ — **DONE**, see Clip review section above. Same on-device caveat
   as folder import.

1. **Rating displayed as `X/10`** everywhere a rating shows (Clip Detail, Reviewed list, anywhere
   else it might surface later) — currently a bare integer. Only remaining gap in the core Clips
   loop; small, isolated change (display formatting only, no schema/logic change).
2. **Automatic Start/End Session flow** — Start Session button → live-tracks start/end/duration/
   clip-count → End Session prompts for W/L/rank/notes. Repository methods already exist and are
   unused.
3. **Home "Statistics summary" + "Current goal/progress"** sections — spec calls for both; neither
   exists on Home yet (Home currently covers pending review / last session / latest achievement
   only).
4. **Developer Mode 7-tap unlock gesture** in Settings → About → Version, instead of a plain nav
   button.
5. **Achievement/milestone create/edit UI** in Developer Mode (`upsert`/`delete` already exist on
   the repository, no screen calls them).
6. **Developer Mode "Database tools" and "Rescan clip folder"** — both named in the original spec,
   neither built. Note: folder-based import now has its own in-screen "Rescan" button
   (`ImportClipActivity`) — worth checking with Sam whether that satisfies this spec line or
   whether Dev Mode still wants its own separate entry point.

---

## SUGGESTED NEXT SESSION ORDER
With the two big Clips-loop items (folder import, review queue) done, the remaining gaps are all
either small polish (#1, rating format — cheap, do this first) or secondary flows/screens (#2
automatic sessions, #3 Home sections, #4–6 Dev Mode). Recommend:
1. **Rating `X/10` format** first — quick, low-risk, closes out the core Clips loop completely.
2. **Automatic session tracking (#2)** next — it's the next-biggest unbuilt user-facing flow and
   the repository-level plumbing already exists, so it's mostly a UI/state-machine task.
3. Then Home sections and Dev Mode items as polish, in whatever order's convenient — none of them
   block each other.

Also worth doing soon, not spec-gap-related: get Sam to run the folder-import and review-queue
flows once on a real device (multi-file folder, background-and-return, Previous/Skip/Next at both
queue ends) so those two can move from "confirmed by code" to fully confirmed like the rest of the
built list.
