-- RL Vault SQLite Schema (Milestone 1)
-- Design notes are in ARCHITECTURE.md

PRAGMA foreign_keys = ON;

CREATE TABLE clips (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path       TEXT NOT NULL UNIQUE,   -- absolute path, never moved/copied
    title           TEXT,
    notes           TEXT,
    rating          INTEGER,                -- numerator only, denominator always /10, can exceed 10
    favorite        INTEGER NOT NULL DEFAULT 0, -- 0/1
    duration_ms     INTEGER,
    created_at       INTEGER NOT NULL,        -- file creation timestamp (epoch millis)
    imported_at      INTEGER NOT NULL,        -- when RL Vault indexed it
    reviewed         INTEGER NOT NULL DEFAULT 0, -- 0 = still in review queue
    session_id       INTEGER,                 -- nullable FK, which session it was imported during
    FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE SET NULL
);

CREATE TABLE mechanics (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name    TEXT NOT NULL UNIQUE          -- e.g. "Flip Reset", "Musty", "Pinch"
);

CREATE TABLE clip_mechanics (
    clip_id     INTEGER NOT NULL,
    mechanic_id INTEGER NOT NULL,
    PRIMARY KEY (clip_id, mechanic_id),
    FOREIGN KEY (clip_id) REFERENCES clips(id) ON DELETE CASCADE,
    FOREIGN KEY (mechanic_id) REFERENCES mechanics(id) ON DELETE CASCADE
);

CREATE TABLE sessions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    is_automatic    INTEGER NOT NULL DEFAULT 0, -- 1 = tracked via Start/End Session flow
    start_time      INTEGER,                    -- epoch millis, null if purely manual w/ only date
    end_time        INTEGER,
    duration_ms     INTEGER,
    date            INTEGER NOT NULL,           -- epoch millis, date of session (manual entries use this)
    wins            INTEGER DEFAULT 0,
    losses          INTEGER DEFAULT 0,
    rank            TEXT,                        -- optional, free text (e.g. "Champ II")
    notes           TEXT,
    clip_count      INTEGER DEFAULT 0            -- denormalized count of clips imported during session
);

CREATE TABLE achievements (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    key             TEXT NOT NULL UNIQUE,   -- stable identifier, e.g. "flip_reset_100"
    title           TEXT NOT NULL,
    description     TEXT,
    condition_json  TEXT NOT NULL,          -- softcoded rule, e.g. {"type":"mechanic_count","mechanic":"Flip Reset","threshold":100}
    unlocked        INTEGER NOT NULL DEFAULT 0,
    unlocked_at     INTEGER,
    is_milestone    INTEGER NOT NULL DEFAULT 0, -- distinguishes "Milestones" (clip count etc) from mechanic-based achievements
    created_by_dev  INTEGER NOT NULL DEFAULT 0  -- 1 if user-authored in Developer Mode vs shipped default
);

CREATE INDEX idx_clips_reviewed ON clips(reviewed);
CREATE INDEX idx_clips_session ON clips(session_id);
CREATE INDEX idx_clip_mechanics_mechanic ON clip_mechanics(mechanic_id);
