-- V4__add_active_key_index
CREATE INDEX idx_post_author_id_status ON post (author_id, status);

ALTER TABLE post
    ADD COLUMN active_key VARCHAR(255)
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ACTIVE' THEN CONCAT(author_id, '_', s2token_id) ELSE NULL END
            ) STORED;

CREATE UNIQUE INDEX uix_posts_active_key ON post (active_key);