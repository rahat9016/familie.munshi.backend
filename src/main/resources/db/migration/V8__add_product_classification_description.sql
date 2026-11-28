-- =====================================================================
-- V8: product_classifications.description (required)
--
-- Existing rows are backfilled with their name so the column can be
-- NOT NULL. Edit those descriptions afterwards as needed.
-- =====================================================================

ALTER TABLE product_classifications
    ADD COLUMN IF NOT EXISTS description text;

UPDATE product_classifications
SET description = name
WHERE description IS NULL
   OR btrim(description) = '';

ALTER TABLE product_classifications
    ALTER COLUMN description SET NOT NULL;
