CREATE INDEX IF NOT EXISTS idx_post_s2token ON post (s2token_id);
CREATE INDEX IF NOT EXISTS idx_post_author_id_status ON post (author_id, status);
