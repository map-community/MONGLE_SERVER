-- V3__add_pending_status_to_post.sql
UPDATE post SET status = 'PENDING' WHERE status = 'UPLOADING';

ALTER TABLE post MODIFY COLUMN status ENUM(
    'PENDING',
    'ACTIVE',
    'EXPIRED',
    'DELETED_BY_USER',
    'DELETED_BY_ADMIN',
    'DELETED_BY_WITHDRAWAL'
) NOT NULL;