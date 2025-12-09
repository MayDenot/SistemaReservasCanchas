
\c reservas_db;

CREATE SCHEMA IF NOT EXISTS reservas;

-- ============================================
--   TABLA USERS
-- ============================================

CREATE TABLE IF NOT EXISTS reservas.users
(
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    user_role  VARCHAR(50)         NOT NULL, -- Enum reemplazado por texto
    name       VARCHAR(255)        NOT NULL,
    phone      VARCHAR(50),
    created_at TIMESTAMP
    );


-- ============================================
--   TABLA CLUBS
-- ============================================

CREATE TABLE IF NOT EXISTS reservas.clubs
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    phone        VARCHAR(50),
    opening_time TIME         NOT NULL,
    closing_time TIME         NOT NULL,
    admin_id     BIGINT       NOT NULL
    );


-- ============================================
--   TABLA COURTS
-- ============================================

CREATE TABLE IF NOT EXISTS reservas.courts
(
    id             SERIAL PRIMARY KEY,
    club_id        BIGINT         NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    type           VARCHAR(50)    NOT NULL, -- Enum -> texto
    price_per_hour NUMERIC(10, 2) NOT NULL,
    is_active      BOOLEAN,

    CONSTRAINT fk_court_club
    FOREIGN KEY (club_id) REFERENCES reservas.clubs (id)
    );


-- ============================================
--   TABLA RESERVATIONS
-- ============================================

CREATE TABLE IF NOT EXISTS reservas.reservations
(
    id             SERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    court_id       BIGINT      NOT NULL,
    club_id        BIGINT      NOT NULL,
    start_time     TIMESTAMP   NOT NULL,
    end_time       TIMESTAMP   NOT NULL,
    status         VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount   NUMERIC(10, 2),
    paid_amount    NUMERIC(10, 2)       DEFAULT 0.00,
    created_at     TIMESTAMP            DEFAULT NOW(),
    updated_at     TIMESTAMP            DEFAULT NOW(),

    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES reservas.users (id),
    CONSTRAINT fk_res_court FOREIGN KEY (court_id) REFERENCES reservas.courts (id),
    CONSTRAINT fk_res_club FOREIGN KEY (club_id) REFERENCES reservas.clubs (id)
    );


-- ============================================
--   TABLA PAYMENTS
-- ============================================

CREATE TABLE IF NOT EXISTS reservas.payments
(
    id                  SERIAL PRIMARY KEY,
    reservation_id      BIGINT         NOT NULL,
    amount              NUMERIC(10, 2) NOT NULL,
    status              VARCHAR(50)    NOT NULL, -- Enum -> texto
    method              VARCHAR(50)    NOT NULL, -- Enum -> texto
    external_payment_id VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,

    CONSTRAINT fk_payment_res
    FOREIGN KEY (reservation_id) REFERENCES reservas.reservations (id)
    );


-- ============================================
--   TABLA NOTIFICATIONS
-- ============================================

CREATE TABLE IF NOT EXISTS reservas.notifications
(
    id              SERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    reservation_id  BIGINT,
    type            VARCHAR(50)  NOT NULL, -- Enum -> texto
    title           VARCHAR(255) NOT NULL,
    message         TEXT,
    channel         VARCHAR(50)  NOT NULL, -- Enum -> texto
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),
    sent_at         TIMESTAMP,
    created_at      TIMESTAMP             DEFAULT NOW(),
    retry_count     INTEGER,
    error_message   VARCHAR(255),

    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES reservas.users (id),
    CONSTRAINT fk_notif_res FOREIGN KEY (reservation_id) REFERENCES reservas.reservations (id)
    );
