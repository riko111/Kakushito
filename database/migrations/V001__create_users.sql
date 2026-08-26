CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    -- Firebase AuthenticationのUID
    firebase_uid VARCHAR(128) NOT NULL,

    -- Firebaseから取得できるユーザー情報
    email VARCHAR(320) NULL,
    display_name VARCHAR(255) NULL,

    -- free / premium
    plan VARCHAR(20) NOT NULL DEFAULT 'free',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_users_firebase_uid (firebase_uid),

    KEY idx_users_email (email),

    CONSTRAINT chk_users_plan
        CHECK (plan IN ('free', 'premium'))
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;