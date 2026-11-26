-- =====================================================================
-- V4: product_classifications display position
--
-- Per-level ordering: sort_order is unique within each level and rows
-- are listed ascending by it. Existing rows are backfilled per level
-- by created_at then name so current data stays consistent.
-- =====================================================================

ALTER TABLE product_classifications
    ADD COLUMN IF NOT EXISTS sort_order integer;

WITH ranked AS (
    SELECT id,
           row_number() OVER (
               PARTITION BY level
               ORDER BY created_at, name
           ) AS rn
    FROM product_classifications
)
UPDATE product_classifications pc
SET sort_order = ranked.rn
FROM ranked
WHERE pc.id = ranked.id
  AND pc.sort_order IS NULL;

ALTER TABLE product_classifications
    ALTER COLUMN sort_order SET NOT NULL;

ALTER TABLE product_classifications
    ADD CONSTRAINT uq_product_classifications_level_sort_order
        UNIQUE (level, sort_order);
