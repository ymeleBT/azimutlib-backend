-- One-off backfill: assign a random-looking cover image URL to every book that
-- doesn't have one yet. Uses picsum.photos' seeded endpoint so each book_id
-- deterministically maps to a distinct placeholder image (no external API key
-- needed; images are fetched client-side by the browser, not by the backend).
-- Run with:
--   mysql -h localhost -P 3306 -u <user> -p<pass> library_db < scripts/seed_book_covers.sql

UPDATE books
SET cover_url = CONCAT('https://picsum.photos/seed/book', id, '/400/600')
WHERE cover_url IS NULL OR cover_url = '';
