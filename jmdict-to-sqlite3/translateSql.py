# import argostranslate.package
# import argostranslate.translate
# import sqlite3
# from tqdm import tqdm
# import time

# # One-time setup: Download and install English->Vietnamese model
# print("Setting up translation model...")
# argostranslate.package.update_package_index()
# available_packages = argostranslate.package.get_available_packages()
# package_to_install = next(
#     filter(lambda x: x.from_code == "en" and x.to_code == "vi", available_packages)
# )
# argostranslate.package.install_from_path(package_to_install.download())

# # Get translator
# installed_languages = argostranslate.translate.get_installed_languages()
# en = next(filter(lambda x: x.code == "en", installed_languages))
# vi = next(filter(lambda x: x.code == "vi", installed_languages))
# translator = en.get_translation(vi)

# print("Translation model ready!")

# # Connect to your database
# DB_PATH = 'C:/Users/NGHIA/Documents/Fresher FPT/Tech/kotlin/japanlib/app/src/main/assets/jpDictionary.db'
# conn = sqlite3.connect(DB_PATH)
# cursor = conn.cursor()

# # Get all English entries that don't have Vietnamese translations yet
# cursor.execute("""
#     SELECT DISTINCT e.entry_id, e.gloss, e.position
#     FROM entry_translation e
#     WHERE e.language_code = 'en'
#     AND NOT EXISTS (
#         SELECT 1 FROM entry_translation v
#         WHERE v.entry_id = e.entry_id
#         AND v.language_code = 'vi'
#     )
# """)

# entries_to_translate = cursor.fetchall()
# total = len(entries_to_translate)

# print(f"Found {total} English entries to translate to Vietnamese")

# if total == 0:
#     print("No entries to translate!")
#     conn.close()
#     exit()

# # Process each entry
# successful = 0
# failed = 0

# for entry_id, gloss, position in tqdm(entries_to_translate, desc="Translating"):
#     try:
#         # Translate gloss (can be NULL)
#         translated_gloss = None
#         if gloss:
#             translated_gloss = translator.translate(gloss)
        
#         # Translate position (can be NULL)
#         translated_position = None
#         if position:
#             translated_position = translator.translate(position)
        
#         # Insert Vietnamese translation
#         cursor.execute("""
#             INSERT INTO entry_translation (entry_id, language_code, gloss, position)
#             VALUES (?, 'vi', ?, ?)
#         """, (entry_id, translated_gloss, translated_position))
        
#         successful += 1
        
#         # Commit every 100 rows for safety
#         if successful % 100 == 0:
#             conn.commit()
            
#     except Exception as e:
#         print(f"\nError translating entry_id {entry_id}: {e}")
#         failed += 1
#         continue

# # Final commit
# conn.commit()
# conn.close()

# print(f"\n✓ Translation complete!")
# print(f"  Successfully translated: {successful}")
# print(f"  Failed: {failed}")
# print(f"  Total Vietnamese entries added: {successful}")


import sqlite3
import re

def remove_vietnamese_tones(s):
    s = s.lower()
    s = re.sub(r"[àáạảãâầấậẩẫăằắặẳẵ]", "a", s)
    s = re.sub(r"[èéẹẻẽêềếệểễ]", "e", s)
    s = re.sub(r"[ìíịỉĩ]", "i", s)
    s = re.sub(r"[òóọỏõôồốộổỗơờớợởỡ]", "o", s)
    s = re.sub(r"[ùúụủũưừứựửữ]", "u", s)
    s = re.sub(r"[ỳýỵỷỹ]", "y", s)
    s = re.sub(r"[đ]", "d", s)
    return s

conn = sqlite3.connect("C:/Users/NGHIA/Documents/Fresher FPT/Tech/kotlin/japanlib/app/src/main/assets/jpDictionary.db")
cur = conn.cursor()

cur.execute("ALTER TABLE entry_translation ADD COLUMN gloss_norm TEXT")

cur.execute("SELECT translationId, gloss FROM entry_translation")
rows = cur.fetchall()

for id, gloss in rows:
    norm = remove_vietnamese_tones(gloss) if gloss else None
    cur.execute(
        "UPDATE entry_translation SET gloss_norm = ? WHERE translationId = ?",
        (norm, id)
    )

conn.commit()
conn.close()