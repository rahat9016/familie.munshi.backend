-- =====================================================================
-- V4: seed product classifications
--
-- Tree: GROUP -> PARENT_CATEGORY -> CATEGORY -> SUB_CATEGORY
-- (SEGMENT is optional and not used by this seed.)
--
-- Every group (Women, Men, Kids) gets its own copy of the same
-- parent categories, categories and sub categories.
--
-- Idempotent: a row with the same name (case-insensitive), level and
-- parent is reused, never duplicated or modified. Existing rows keep
-- their ids, so nothing that references them breaks.
--
-- Codes follow the application's rule: NAME, then NAME-2, NAME-3 ...
-- =====================================================================

CREATE FUNCTION pg_temp.seed_product_classification(
    p_name   text,
    p_level  text,
    p_parent uuid
) RETURNS uuid
LANGUAGE plpgsql
AS $fn$
DECLARE
    v_id     uuid;
    v_base   text;
    v_code   text;
    v_suffix int := 2;
BEGIN
    SELECT id
    INTO v_id
    FROM product_classifications
    WHERE lower(trim(name)) = lower(trim(p_name))
      AND level = p_level
      AND parent_id IS NOT DISTINCT FROM p_parent
    ORDER BY is_active DESC, created_at
    LIMIT 1;

    IF v_id IS NOT NULL THEN
        RETURN v_id;
    END IF;

    -- Same normalization as ProductClassificationService.normalizeCode,
    -- truncated so a "-NNN" suffix still fits in varchar(50).
    v_base := trim(BOTH '_' FROM regexp_replace(upper(trim(p_name)), '[^A-Z0-9]+', '_', 'g'));
    v_base := trim(BOTH '_' FROM left(v_base, 45));
    v_code := v_base;

    WHILE EXISTS (
        SELECT 1 FROM product_classifications WHERE upper(code) = upper(v_code)
    ) LOOP
        v_code := v_base || '-' || v_suffix;
        v_suffix := v_suffix + 1;
    END LOOP;

    v_id := gen_random_uuid();

    INSERT INTO product_classifications (
        id, code, created_at, icon, is_active, level, name, updated_at, parent_id
    ) VALUES (
        v_id, v_code, now(), NULL, true, p_level, trim(p_name), now(), p_parent
    );

    RETURN v_id;
END;
$fn$;

DO $seed$
DECLARE
    v_groups text[] := ARRAY['Women', 'Men', 'Kids'];

    v_parent_categories text[] := ARRAY[
        'Custom-Teamwear', 'Rent-a-Dress', 'Golden-Draw', 'Clothing', 'Basics',
        'Sportswear', 'Streetwear', 'Workwear', 'Accessories', 'Baggage', 'Shoes'
    ];

    -- Parent categories with their own menu.
    v_menus jsonb := '{
      "Custom-Teamwear": [
        {"name": "For Businesses & Promotions", "items": [
          "Corporate Swag", "Trade Show Merch & Signage", "Marketing Campaigns & Giveaways",
          "Promo & Advertising Merch", "Client Gifts", "Branded Staff Apparel",
          "Bulk order for wholesale Business", "Employee Recognition & Gifts",
          "Employee Team Building Swag", "Content Creators", "Small Businesses & Uniforms"
        ]},
        {"name": "For Group & Events", "items": [
          "Sports Jerseys", "Personalized Gifts", "Weddings", "Family Events", "Friend Events",
          "Clubs & Organisations", "Kindergarten to 12th Class", "Colleges & Universities",
          "Teacher Appreciation", "Hospital services", "Festival & Religious Events"
        ]},
        {"name": "Designing Tool", "items": [
          "T-shirt", "Tanktop", "Polo Shirt", "Sweatshirt", "Hoodies", "Blazer", "Socks",
          "Skirts", "short Trousers", "Flags", "Jerseys"
        ]},
        {"name": "Accessories", "items": [
          "Socks", "Belts", "Hats & Caps", "Flags", "Back pack", "Bags", "Pens", "Mugs",
          "Water Bottle", "Key Ring", "Gift Card"
        ]}
      ],
      "Rent-a-Dress": [
        {"name": "The Essential Closet", "items": [
          "Everyday", "Formal Wear", "Office Wear", "Matching Sets"
        ]},
        {"name": "The Occasion Wardrobe", "items": [
          "Vacations", "Weddings", "Traditional", "Summer/Winter Outfitting"
        ]},
        {"name": "The Lifestyle Edit", "items": [
          "Exercise Time", "Sports Day", "Maternity Cloths", "Religionals"
        ]},
        {"name": "The Family Vault", "items": [
          "Kids", "Boys", "Girls", "Ornaments"
        ]}
      ]
    }'::jsonb;

    -- Shared menu for every other parent category.
    v_shared_menu jsonb := '[
      {"name": "Tops & Indoor", "items": [
        "Blouses", "Shirts", "T-shirts", "Polo shirts", "Sweatshirts", "Sweatshirt Jacke",
        "Indoor jackets", "Indoor Blazer", "Indoor-Sakko", "Indoor-Weste"
      ]},
      {"name": "Outdoor & Knitwear", "items": [
        "Outdoor jackets", "Outdoor-Coat", "Outdoor-Weste", "Outdoor-Poncho", "Outdoor-Umhang",
        "Knitwear", "Knitted sweater", "Knitted Vest", "Knitted cardigan", "Knitted twinset"
      ]},
      {"name": "Bottoms & More", "items": [
        "Pants", "Jeans pants", "Skirts", "One-pieces/dresses", "Underwear", "Swimwear",
        "Accessories", "Socks/Hosiery", "Daywear", "Hats"
      ]},
      {"name": "Home & Specials", "items": [
        "Curtains", "Home Cloths", "Shoes", "Fine Jewelry", "Fashion Jewelry", "Bags",
        "Fragrances", "Flags", "Religious", "Traditionals"
      ]}
    ]'::jsonb;

    v_group           text;
    v_parent_category text;
    v_category        jsonb;
    v_sub_category    text;
    v_group_id        uuid;
    v_pc_id           uuid;
    v_category_id     uuid;
BEGIN
    FOREACH v_group IN ARRAY v_groups LOOP
        v_group_id := pg_temp.seed_product_classification(v_group, 'GROUP', NULL);

        FOREACH v_parent_category IN ARRAY v_parent_categories LOOP
            v_pc_id := pg_temp.seed_product_classification(
                v_parent_category, 'PARENT_CATEGORY', v_group_id
            );

            FOR v_category IN
                SELECT value
                FROM jsonb_array_elements(COALESCE(v_menus -> v_parent_category, v_shared_menu))
            LOOP
                v_category_id := pg_temp.seed_product_classification(
                    v_category ->> 'name', 'CATEGORY', v_pc_id
                );

                FOR v_sub_category IN
                    SELECT jsonb_array_elements_text(v_category -> 'items')
                LOOP
                    PERFORM pg_temp.seed_product_classification(
                        v_sub_category, 'SUB_CATEGORY', v_category_id
                    );
                END LOOP;
            END LOOP;
        END LOOP;
    END LOOP;
END
$seed$;

DROP FUNCTION pg_temp.seed_product_classification(text, text, uuid);
