CREATE TABLE documents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    user_id BIGINT UNSIGNED NOT NULL,

    -- ユーザーのストレージ
    -- 例: google_drive / onedrive / dropbox
    storage_provider VARCHAR(50) NOT NULL,

    -- ストレージ側のファイルID
    storage_file_id VARCHAR(512) NOT NULL,

    -- 表示用ファイル名
    file_name VARCHAR(512) NOT NULL,

    -- PDF内容の識別・変更検知用
    file_hash CHAR(64) NULL,
    file_size BIGINT UNSIGNED NULL,
    file_modified_at DATETIME(6) NULL,

    -- Kakushito上での表示名
    title VARCHAR(512) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_documents_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_documents_storage_file (
        user_id,
        storage_provider,
        storage_file_id
    ),

    KEY idx_documents_user_id (user_id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;