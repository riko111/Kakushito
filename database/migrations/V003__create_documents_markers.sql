CREATE TABLE document_markers (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    document_id BIGINT UNSIGNED NOT NULL,

    -- PDFページ番号（1始まり）
    page_number INT UNSIGNED NOT NULL,

    -- PDF座標系
    -- 左上を原点とする
    x DECIMAL(12,6) NOT NULL,
    y DECIMAL(12,6) NOT NULL,
    width DECIMAL(12,6) NOT NULL,
    height DECIMAL(12,6) NOT NULL,

    -- マーカー種類
    marker_type VARCHAR(50) NOT NULL DEFAULT 'hide',

    -- 色
    color VARCHAR(20) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_document_markers_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    KEY idx_document_markers_document_id (
        document_id
    ),

    KEY idx_document_markers_document_page (
        document_id,
        page_number
    )
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;