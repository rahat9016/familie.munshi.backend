-- =====================================================================
-- V6: colorways + colorway_images
-- code is system-generated (CLR-XXXXXX) and unique.
-- Only marketing_name + name required on create; rest defaulted.
-- color_standard is free text stored uppercase, default PANTONE.
-- Images ordered by sort_order; position 0 acts as cover.
-- =====================================================================

CREATE TABLE IF NOT EXISTS colorways (
    id                    uuid                        NOT NULL,
    code                  varchar(20)                 NOT NULL,
    marketing_name        varchar(150)                NOT NULL,
    name                  varchar(150)                NOT NULL,
    color_specification   varchar(255),
    description           text,
    color_standard        varchar(20)                 NOT NULL DEFAULT 'PANTONE',
    color_reference       varchar(50),
    hex_code              varchar(7),
    is_active             boolean                     NOT NULL DEFAULT true,
    in_theme              boolean                     NOT NULL DEFAULT false,
    sustainability_label  boolean                     NOT NULL DEFAULT false,
    plan_sms              boolean                     NOT NULL DEFAULT false,
    plan_3d_sms           boolean                     NOT NULL DEFAULT false,
    actual_sms            boolean                     NOT NULL DEFAULT false,
    start_date            date,
    end_date              date,
    clearance_date        date,
    created_at            timestamp(6) with time zone NOT NULL,
    updated_at            timestamp(6) with time zone NOT NULL,
    CONSTRAINT colorways_pkey PRIMARY KEY (id),
    CONSTRAINT uk_colorways_code UNIQUE (code),
    CONSTRAINT chk_colorways_hex_code
        CHECK (hex_code IS NULL OR hex_code ~ '^#[0-9A-F]{6}$'),
    CONSTRAINT chk_colorways_dates CHECK (
        start_date IS NULL OR end_date IS NULL OR end_date >= start_date
    )
);

CREATE INDEX IF NOT EXISTS idx_colorways_is_active      ON colorways (is_active);
CREATE INDEX IF NOT EXISTS idx_colorways_in_theme       ON colorways (in_theme);
CREATE INDEX IF NOT EXISTS idx_colorways_color_standard ON colorways (color_standard);
CREATE INDEX IF NOT EXISTS idx_colorways_code_pattern   ON colorways (code varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_colorways_created_at     ON colorways (created_at DESC);

CREATE TABLE IF NOT EXISTS colorway_images (
    id           uuid                        NOT NULL,
    colorway_id  uuid                        NOT NULL,
    image_key    varchar(255)                NOT NULL,
    sort_order   integer                     NOT NULL,
    created_at   timestamp(6) with time zone NOT NULL,
    CONSTRAINT colorway_images_pkey PRIMARY KEY (id),
    CONSTRAINT fk_colorway_images_colorway
        FOREIGN KEY (colorway_id) REFERENCES colorways (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_colorway_images_colorway_id ON colorway_images (colorway_id, sort_order);
