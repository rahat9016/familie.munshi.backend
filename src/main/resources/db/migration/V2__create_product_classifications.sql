-- =====================================================================
-- V2: product_classifications
-- IF NOT EXISTS: some databases already have this table (created by
-- Hibernate ddl-auto: update before Flyway). On those it is a no-op.
-- =====================================================================

CREATE TABLE IF NOT EXISTS product_classifications (
    id         uuid                        NOT NULL,
    code       varchar(50)                 NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    icon       varchar(100),
    is_active  boolean                     NOT NULL,
    level      varchar(30)                 NOT NULL,
    name       varchar(150)                NOT NULL,
    updated_at timestamp(6) with time zone,
    parent_id  uuid,
    CONSTRAINT product_classifications_pkey PRIMARY KEY (id),
    CONSTRAINT product_classifications_level_check
        CHECK (level IN ('GROUP', 'PARENT_CATEGORY', 'CATEGORY', 'SEGMENT', 'SUB_CATEGORY')),
    CONSTRAINT fki2jsh45x141ke8hv1w2285dkc
        FOREIGN KEY (parent_id) REFERENCES product_classifications (id)
);
