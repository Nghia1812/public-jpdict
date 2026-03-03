import sqlite3
import json

JSON_PATH = "all.json"
DB_PATH = "C:/Users/NGHIA/Documents/Fresher FPT/Tech/kotlin/japanlib/app/src/main/assets/jpDictionary.db"

LEVEL_MAP = {
    1: "n1",
    2: "n2",
    3: "n3",
    4: "n4",
    5: "n5"
}

DEFAULT_STATE = "NOT_LEARNT_YET"

conn = sqlite3.connect(DB_PATH)
cur = conn.cursor()

# Ensure tables exist
cur.executescript("""
CREATE TABLE IF NOT EXISTS jlpt_word_list (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS jlpt_word_entry (
    list_id TEXT NOT NULL,
    entry_id INTEGER NOT NULL,
    learning_state TEXT NOT NULL,
    PRIMARY KEY (list_id, entry_id),
    FOREIGN KEY (list_id) REFERENCES jlpt_word_list(id) ON DELETE CASCADE,
    FOREIGN KEY (entry_id) REFERENCES entry_new(id) ON DELETE CASCADE
);
""")

# Insert JLPT lists
for level, list_id in LEVEL_MAP.items():
    cur.execute(
        "INSERT OR IGNORE INTO jlpt_word_list (id, name) VALUES (?, ?)",
        (list_id, list_id.upper())
    )

# Process JSON
with open(JSON_PATH, "r", encoding="utf-8") as f:

    data_list = json.load(f)

    for data in data_list:

        word = data.get("word")
        reading = data.get("furigana")
        level = data.get("level")

        if level not in LEVEL_MAP:
            continue

        list_id = LEVEL_MAP[level]
        entry_id = None

        # 1. Match kanji
        cur.execute(
            "SELECT id, reading FROM entry_new WHERE kanji = ?",
            (word,)
        )
        rows = cur.fetchall()

        if rows:
            # filter by reading
            filtered = [
                r[0] for r in rows
                if r[1] == reading
            ]
            if filtered:
                entry_id = filtered[0]

        # 2. Fallback: match reading only
        if entry_id is None and reading:
            cur.execute(
                "SELECT id FROM entry_new WHERE reading = ? LIMIT 1",
                (reading,)
            )
            row = cur.fetchone()
            if row:
                entry_id = row[0]

        # 3. Skip if still not found
        if entry_id is None:
            continue

        # Insert mapping
        cur.execute(
            """
            INSERT OR IGNORE INTO jlpt_word_entry
            (list_id, entry_id, learning_state)
            VALUES (?, ?, ?)
            """,
            (list_id, entry_id, DEFAULT_STATE)
        )

conn.commit()
conn.close()

print("JLPT import (strict kanji + reading) completed.")
