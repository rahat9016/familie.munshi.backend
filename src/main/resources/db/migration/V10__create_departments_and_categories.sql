-- =====================================================================
-- V10: departments + categories
-- department name unique (case-insensitive).
-- category name unique per department (case-insensitive).
-- numberOfStyles is computed from styles, not stored.
-- =====================================================================

CREATE TABLE IF NOT EXISTS departments (
    id          uuid                        NOT NULL,
    name        varchar(100)                NOT NULL,
    is_active   boolean                     NOT NULL DEFAULT true,
    created_at  timestamp(6) with time zone NOT NULL,
    updated_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT departments_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_departments_name_lower ON departments (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_departments_active ON departments (is_active);
CREATE INDEX IF NOT EXISTS idx_departments_created_at ON departments (created_at);

CREATE TABLE IF NOT EXISTS categories (
    id             uuid                        NOT NULL,
    department_id  uuid                        NOT NULL,
    name           varchar(100)                NOT NULL,
    is_active      boolean                     NOT NULL DEFAULT true,
    created_at     timestamp(6) with time zone NOT NULL,
    updated_at     timestamp(6) with time zone NOT NULL,
    CONSTRAINT categories_pkey PRIMARY KEY (id),
    CONSTRAINT fk_categories_department FOREIGN KEY (department_id)
        REFERENCES departments (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_categories_department_name_lower
    ON categories (department_id, LOWER(name));
CREATE INDEX IF NOT EXISTS idx_categories_department_id ON categories (department_id);
CREATE INDEX IF NOT EXISTS idx_categories_active ON categories (is_active);
CREATE INDEX IF NOT EXISTS idx_categories_created_at ON categories (created_at);
