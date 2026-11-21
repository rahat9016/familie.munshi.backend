-- =====================================================================
-- V6: add SEGMENT level to seeded product classifications
--
-- Tree becomes: GROUP -> PARENT_CATEGORY -> CATEGORY -> SEGMENT -> SUB_CATEGORY
--
-- For every active CATEGORY whose parent category and name match the menu
-- below (under any group):
--   1. Create each SEGMENT under the category (reused if it already exists).
--   2. Move each listed SUB_CATEGORY from the category to its segment.
--      Sub categories already under the segment are left alone.
--      Missing sub categories are created under the segment.
--
-- Rows are matched by name (case-insensitive), level and parent.
-- Sub categories not listed below stay where they are.
-- Active/inactive state of existing rows is never changed.
-- Idempotent: running it again changes nothing.
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
    -- Parent categories with their own menu, keyed by lower-case name.
    v_menus jsonb := '{
      "custom-teamwear": [
        {"name": "For Businesses & Promotions", "segments": [
          {"name": "Corporate & Staff", "items": [
            "Corporate Swag", "Branded Staff Apparel", "Small Businesses & Uniforms",
            "Bulk order for wholesale Business"]},
          {"name": "Marketing & Promotion", "items": [
            "Trade Show Merch & Signage", "Marketing Campaigns & Giveaways",
            "Promo & Advertising Merch", "Content Creators"]},
          {"name": "Gifts & Recognition", "items": [
            "Client Gifts", "Employee Recognition & Gifts", "Employee Team Building Swag"]}
        ]},
        {"name": "For Group & Events", "segments": [
          {"name": "Sports & Clubs", "items": [
            "Sports Jerseys", "Clubs & Organisations"]},
          {"name": "Celebrations", "items": [
            "Personalized Gifts", "Weddings", "Family Events", "Friend Events",
            "Festival & Religious Events"]},
          {"name": "Education & Institutions", "items": [
            "Kindergarten to 12th Class", "Colleges & Universities", "Teacher Appreciation",
            "Hospital services"]}
        ]},
        {"name": "Designing Tool", "segments": [
          {"name": "Tops", "items": ["T-shirt", "Tanktop", "Polo Shirt", "Jerseys"]},
          {"name": "Sweats & Outerwear", "items": ["Sweatshirt", "Hoodies", "Blazer"]},
          {"name": "Bottoms & Socks", "items": ["Skirts", "short Trousers", "Socks"]},
          {"name": "Flags", "items": ["Flags"]}
        ]},
        {"name": "Accessories", "segments": [
          {"name": "Wearables", "items": ["Socks", "Belts", "Hats & Caps"]},
          {"name": "Bags", "items": ["Back pack", "Bags"]},
          {"name": "Promo Items", "items": ["Pens", "Mugs", "Water Bottle", "Key Ring", "Flags"]},
          {"name": "Gift Cards", "items": ["Gift Card"]}
        ]}
      ],
      "rent-a-dress": [
        {"name": "The Essential Closet", "segments": [
          {"name": "Daily", "items": ["Everyday", "Matching Sets"]},
          {"name": "Formal & Office", "items": ["Formal Wear", "Office Wear"]}
        ]},
        {"name": "The Occasion Wardrobe", "segments": [
          {"name": "Celebrations", "items": ["Weddings", "Traditional"]},
          {"name": "Travel & Season", "items": ["Vacations", "Summer/Winter Outfitting"]}
        ]},
        {"name": "The Lifestyle Edit", "segments": [
          {"name": "Active", "items": ["Exercise Time", "Sports Day"]},
          {"name": "Life Moments", "items": ["Maternity Cloths", "Religionals"]}
        ]},
        {"name": "The Family Vault", "segments": [
          {"name": "Children", "items": ["Kids", "Boys", "Girls"]},
          {"name": "Jewelry", "items": ["Ornaments"]}
        ]}
      ]
    }'::jsonb;

    -- Parent categories sharing one menu (lower-case).
    v_shared_parent_categories text[] := ARRAY[
        'golden-draw', 'clothing', 'basics', 'sportswear', 'streetwear',
        'workwear', 'accessories', 'baggage', 'shoes'
    ];

    v_shared_menu jsonb := '[
      {"name": "Tops & Indoor", "segments": [
        {"name": "Shirts & Tops", "items": ["Blouses", "Shirts", "T-shirts", "Polo shirts"]},
        {"name": "Sweatwear", "items": ["Sweatshirts", "Sweatshirt Jacke"]},
        {"name": "Indoor Jackets & Tailoring", "items": [
          "Indoor jackets", "Indoor Blazer", "Indoor-Sakko", "Indoor-Weste"]}
      ]},
      {"name": "Outdoor & Knitwear", "segments": [
        {"name": "Outerwear", "items": ["Outdoor jackets", "Outdoor-Coat", "Outdoor-Weste"]},
        {"name": "Capes & Ponchos", "items": ["Outdoor-Poncho", "Outdoor-Umhang"]},
        {"name": "Knitwear", "items": [
          "Knitwear", "Knitted sweater", "Knitted Vest", "Knitted cardigan", "Knitted twinset"]}
      ]},
      {"name": "Bottoms & More", "segments": [
        {"name": "Bottoms", "items": ["Pants", "Jeans pants", "Skirts"]},
        {"name": "Dresses & Daywear", "items": ["One-pieces/dresses", "Daywear"]},
        {"name": "Underwear, Swim & Hosiery", "items": ["Underwear", "Swimwear", "Socks/Hosiery"]},
        {"name": "Accessories", "items": ["Accessories", "Hats"]}
      ]},
      {"name": "Home & Specials", "segments": [
        {"name": "Home Textiles", "items": ["Curtains", "Home Cloths"]},
        {"name": "Shoes & Bags", "items": ["Shoes", "Bags"]},
        {"name": "Jewelry & Fragrance", "items": ["Fine Jewelry", "Fashion Jewelry", "Fragrances"]},
        {"name": "Cultural & Religious", "items": ["Flags", "Religious", "Traditionals"]}
      ]}
    ]'::jsonb;

    v_category       record;
    v_menu           jsonb;
    v_category_spec  jsonb;
    v_segment_spec   jsonb;
    v_item           text;
    v_segment_id     uuid;
    v_sub_id         uuid;
    v_sub_parent     uuid;
BEGIN
    FOR v_category IN
        SELECT c.id, c.name, lower(trim(pc.name)) AS parent_category
        FROM product_classifications c
        JOIN product_classifications pc ON pc.id = c.parent_id
        WHERE c.level = 'CATEGORY'
          AND pc.level = 'PARENT_CATEGORY'
          AND c.is_active = true
    LOOP
        v_menu := CASE
            WHEN v_menus ? v_category.parent_category
                THEN v_menus -> v_category.parent_category
            WHEN v_category.parent_category = ANY (v_shared_parent_categories)
                THEN v_shared_menu
        END;

        CONTINUE WHEN v_menu IS NULL;

        SELECT value
        INTO v_category_spec
        FROM jsonb_array_elements(v_menu)
        WHERE lower(value ->> 'name') = lower(trim(v_category.name))
        LIMIT 1;

        CONTINUE WHEN v_category_spec IS NULL;

        FOR v_segment_spec IN
            SELECT value FROM jsonb_array_elements(v_category_spec -> 'segments')
        LOOP
            v_segment_id := pg_temp.seed_product_classification(
                v_segment_spec ->> 'name', 'SEGMENT', v_category.id
            );

            FOR v_item IN
                SELECT jsonb_array_elements_text(v_segment_spec -> 'items')
            LOOP
                -- Prefer a sub category already under the segment,
                -- otherwise take the one still directly under the category.
                v_sub_id := NULL;

                SELECT id, parent_id
                INTO v_sub_id, v_sub_parent
                FROM product_classifications
                WHERE level = 'SUB_CATEGORY'
                  AND lower(trim(name)) = lower(trim(v_item))
                  AND parent_id IN (v_segment_id, v_category.id)
                ORDER BY (parent_id = v_segment_id) DESC, is_active DESC, created_at
                LIMIT 1;

                IF v_sub_id IS NULL THEN
                    PERFORM pg_temp.seed_product_classification(
                        v_item, 'SUB_CATEGORY', v_segment_id
                    );
                ELSIF v_sub_parent <> v_segment_id THEN
                    UPDATE product_classifications
                    SET parent_id  = v_segment_id,
                        updated_at = now()
                    WHERE id = v_sub_id;
                END IF;
            END LOOP;
        END LOOP;
    END LOOP;
END
$seed$;

DROP FUNCTION pg_temp.seed_product_classification(text, text, uuid);
