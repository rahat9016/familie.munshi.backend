-- =====================================================================
-- V5: brands
-- Flat resource. sort_order is unique across all brands and rows are
-- listed ascending by it. title is unique.
-- =====================================================================

CREATE TABLE IF NOT EXISTS brands (
    id          uuid                        NOT NULL,
    title       varchar(150)                NOT NULL,
    description text,
    logo_url    varchar(255)                NOT NULL,
    sort_order  integer                     NOT NULL,
    is_active   boolean                     NOT NULL,
    created_at  timestamp(6) with time zone NOT NULL,
    updated_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT brands_pkey PRIMARY KEY (id),
    CONSTRAINT uk_brands_title UNIQUE (title),
    CONSTRAINT uq_brands_sort_order UNIQUE (sort_order)
);

CREATE INDEX IF NOT EXISTS idx_brands_is_active ON brands (is_active);
