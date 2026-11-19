-- =====================================================================
-- V3: product_classifications indexes
-- IF NOT EXISTS: safe on databases where Hibernate already created them.
-- =====================================================================

CREATE INDEX IF NOT EXISTS idx_product_classifications_parent
    ON product_classifications (parent_id);

CREATE INDEX IF NOT EXISTS idx_product_classifications_level
    ON product_classifications (level);

CREATE INDEX IF NOT EXISTS idx_product_classifications_active
    ON product_classifications (is_active);
