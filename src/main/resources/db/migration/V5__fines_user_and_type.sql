-- Decouple fines from loans so a fine can be issued without a return event
-- (manual fines), and add a type discriminator so fines don't need to be
-- classified by parsing the free-text `reason` string (lost/damaged copies
-- also need a machine-readable type).

ALTER TABLE fines ADD COLUMN user_id BIGINT NULL AFTER loan_id;

UPDATE fines f
    JOIN loans l ON l.id = f.loan_id
    SET f.user_id = l.user_id;

ALTER TABLE fines MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE fines ADD CONSTRAINT fk_fines_user FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX idx_fines_user ON fines(user_id);

ALTER TABLE fines MODIFY COLUMN loan_id BIGINT NULL;

ALTER TABLE fines ADD COLUMN type ENUM('LATE','LOST','DAMAGE','MANUAL') NOT NULL DEFAULT 'LATE' AFTER loan_id;
