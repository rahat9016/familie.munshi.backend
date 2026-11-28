-- =====================================================================
-- V9: seasons
-- season name unique (case-insensitive).
-- status stored as enum name, default ON_PLANNING.
-- numberOfStyles is computed from styles, not stored.
-- =====================================================================

CREATE TABLE IF NOT EXISTS seasons (
    id          uuid                        NOT NULL,
    season      varchar(100)                NOT NULL,
    status      varchar(20)                 NOT NULL DEFAULT 'ON_PLANNING',
    created_at  timestamp(6) with time zone NOT NULL,
    updated_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT seasons_pkey PRIMARY KEY (id),
    CONSTRAINT ck_seasons_status CHECK (status IN ('ON_PLANNING', 'ON_CONCEPT', 'ON_PROCESS', 'COMPLETED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_seasons_season_lower ON seasons (LOWER(season));
CREATE INDEX IF NOT EXISTS idx_seasons_status ON seasons (status);
CREATE INDEX IF NOT EXISTS idx_seasons_created_at ON seasons (created_at);
