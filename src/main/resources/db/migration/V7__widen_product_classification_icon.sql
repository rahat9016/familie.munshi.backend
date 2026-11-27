-- =====================================================================
-- V7: product_classifications.icon
--
-- One column, two kinds of value:
--   * S3 storage key of an uploaded icon file
--     ("public/product-classifications/icons/<uuid>.svg")
--   * lucide-react icon name ("shopping-cart")
-- Storage keys exceed the old 100 character limit, so widen it.
-- =====================================================================

ALTER TABLE product_classifications
    ALTER COLUMN icon TYPE varchar(255);
