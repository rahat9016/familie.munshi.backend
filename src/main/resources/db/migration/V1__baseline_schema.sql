-- =====================================================================
-- V1: Baseline schema
-- Generated from the schema Hibernate built (ddl-auto: update) before
-- Flyway was introduced. Constraint names are kept identical so that
-- existing (baselined) databases and fresh databases stay in sync.
--
-- Existing databases skip this file via baseline-on-migrate (version 1).
-- Fresh databases run it.
-- =====================================================================

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id              uuid                        NOT NULL,
    created_at      timestamp(6) with time zone NOT NULL,
    email           varchar(150)                NOT NULL,
    first_name      varchar(120)                NOT NULL,
    is_active       boolean                     NOT NULL,
    last_name       varchar(120)                NOT NULL,
    password        varchar(255)                NOT NULL,
    updated_at      timestamp(6) with time zone NOT NULL,
    phone           varchar(20),
    profile_picture varchar(255),
    salt            varchar(255)                NOT NULL,
    CONSTRAINT users_pkey    PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE INDEX idx_user_email      ON users (email);
CREATE INDEX idx_user_first_name ON users (first_name);
CREATE INDEX idx_user_last_name  ON users (last_name);
CREATE INDEX idx_user_is_active  ON users (is_active);

-- ---------------------------------------------------------------------
-- branches
-- ---------------------------------------------------------------------
CREATE TABLE branches (
    id         uuid                        NOT NULL,
    address    text,
    code       varchar(255)                NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    is_active  boolean                     NOT NULL,
    logo_url   varchar(255),
    name       varchar(255)                NOT NULL,
    phone      varchar(255),
    updated_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT branches_pkey  PRIMARY KEY (id),
    CONSTRAINT uk_branch_code UNIQUE (code),
    CONSTRAINT uk_branch_name UNIQUE (name)
);

CREATE INDEX idx_branch_code      ON branches (code);
CREATE INDEX idx_branch_name      ON branches (name);
CREATE INDEX idx_branch_is_active ON branches (is_active);

-- ---------------------------------------------------------------------
-- permissions
-- ---------------------------------------------------------------------
CREATE TABLE permissions (
    name        varchar(255)                NOT NULL,
    created_at  timestamp(6) with time zone,
    description varchar(1000),
    updated_at  timestamp(6) with time zone,
    id          uuid                        NOT NULL,
    CONSTRAINT permissions_pkey           PRIMARY KEY (id),
    CONSTRAINT ukpnvtwliis6p05pn6i3ndjrqt2 UNIQUE (name)
);

-- ---------------------------------------------------------------------
-- roles
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    id          uuid                        NOT NULL,
    description text,
    is_active   boolean                     NOT NULL,
    is_system   boolean                     NOT NULL,
    name        varchar(100)                NOT NULL,
    branch_id   uuid,
    created_at  timestamp(6) with time zone NOT NULL,
    scope       varchar(20)                 NOT NULL,
    updated_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT roles_pkey        PRIMARY KEY (id),
    CONSTRAINT roles_scope_check CHECK (scope IN ('PLATFORM', 'BRANCH')),
    CONSTRAINT fkrd3iyw6bjkyc6qoddupccxe07
        FOREIGN KEY (branch_id) REFERENCES branches (id)
);

CREATE INDEX idx_roles_branch    ON roles (branch_id);
CREATE INDEX idx_roles_is_active ON roles (is_active);
CREATE INDEX idx_roles_scope     ON roles (scope);

-- ---------------------------------------------------------------------
-- role_permissions (join table)
-- ---------------------------------------------------------------------
CREATE TABLE role_permissions (
    role_id       uuid NOT NULL,
    permission_id uuid NOT NULL,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

-- ---------------------------------------------------------------------
-- user_platform_roles
-- ---------------------------------------------------------------------
CREATE TABLE user_platform_roles (
    id         uuid                        NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    role_id    uuid                        NOT NULL,
    user_id    uuid                        NOT NULL,
    CONSTRAINT user_platform_roles_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_platform_role    UNIQUE (user_id, role_id),
    CONSTRAINT fk9bgabgadi17xmg5py5v3lyhd5
        FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fkhehfsmme7f2wxm2m65b69154l
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_platform_roles_role ON user_platform_roles (role_id);

-- ---------------------------------------------------------------------
-- user_branches (membership)
-- ---------------------------------------------------------------------
CREATE TABLE user_branches (
    id         uuid                        NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    branch_id  uuid                        NOT NULL,
    user_id    uuid                        NOT NULL,
    CONSTRAINT user_branches_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_branch     UNIQUE (user_id, branch_id),
    CONSTRAINT fkg2946lycn8eakmt5i6l0xaq53
        FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fktdeftymo1mk999qigob1ji2p0
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_branches_branch ON user_branches (branch_id);

-- ---------------------------------------------------------------------
-- user_branch_roles
-- ---------------------------------------------------------------------
CREATE TABLE user_branch_roles (
    id         uuid                        NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    branch_id  uuid                        NOT NULL,
    role_id    uuid                        NOT NULL,
    user_id    uuid                        NOT NULL,
    CONSTRAINT user_branch_roles_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_branch_role    UNIQUE (user_id, branch_id, role_id),
    CONSTRAINT fkfrmo2tqqtf82buyxhtnqnt6a7
        FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fkouta0qfsfbx8d138k9ufdkjjr
        FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fkpyr20m44wtmxio1kuecls25ai
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_ubr_user_branch ON user_branch_roles (user_id, branch_id);
CREATE INDEX idx_ubr_branch_role ON user_branch_roles (branch_id, role_id);
