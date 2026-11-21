-- =====================================================================
-- V5: reactivate parents of active product classifications
--
-- V4 reuses existing rows by name, level and parent, including inactive
-- ones. On databases where a group (e.g. Women, Men, Kids) was inactive,
-- V4 seeded active children under an inactive parent, hiding the whole
-- branch from roots/tree while children still appear by level.
--
-- Fix: every ancestor of an active classification becomes active again.
-- Safe: the API blocks deleting a row with active children, so an
-- inactive row with active descendants only happens through seeding.
-- No-op on databases where the tree is already consistent.
-- =====================================================================

WITH RECURSIVE ancestors AS (
    SELECT parent_id AS id
    FROM product_classifications
    WHERE is_active = true
      AND parent_id IS NOT NULL

    UNION

    SELECT p.parent_id
    FROM product_classifications p
    JOIN ancestors a ON p.id = a.id
    WHERE p.parent_id IS NOT NULL
)
UPDATE product_classifications
SET is_active  = true,
    updated_at = now()
WHERE is_active = false
  AND id IN (SELECT id FROM ancestors);
