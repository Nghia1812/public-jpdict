import csv

# Path to your database and TSV file
DB_PATH = "C:/Users/NGHIA/Documents/Fresher FPT/Tech/kotlin/japanlib/app/src/main/assets/jpDict.db"
TSV_PATH = "C:/Users/NGHIA/Downloads/Sentence pairs in Japanese-English.tsv"

# Connect to database
conn = sqlite3.connect(DB_PATH)
cursor = conn.cursor()

# Fetch all reading entries to memory
cursor.execute("SELECT id, reading FROM entry")
entries = cursor.fetchall()  # List of (id, kanji)

# Helper: check if a sentence contains the reading
def match_entry(sentence, entries):
    for entry_id, reading in entries:
        if (reading and reading in sentence):
            return entry_id
    return None  # No match

# Process the TSV
with open(TSV_PATH, 'r', encoding='utf-8') as f:
    reader = csv.reader(f, delimiter='\t')
    count = 0
    for row in reader:
        if len(row) != 4:
            continue
        jp_id, jp_text, en_id, en_text = row
        jp_id = int(jp_id.strip().lstrip('\ufeff'))
        en_id = int(en_id.strip().lstrip('\ufeff'))
        jp_text = jp_text.strip()
        en_text = en_text.strip()

        entry_id = match_entry(jp_text, entries)
        if entry_id:
            cursor.execute("""
                INSERT INTO ExampleSentences (entry_id, jp_sentence_id, en_sentence_id, sentence_ja, sentence_en)
                VALUES (?, ?, ?, ?, ?)
            """, (entry_id, jp_id, en_id, jp_text, en_text))
            count += 1

conn.commit()
conn.close()
print(f"✅ Imported {count} sentence pairs linked to dictionary entries.")
