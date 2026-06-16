ALTER TABLE vehicle
    ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE vehicle SET created_at = NOW(), updated_at = NOW();

ALTER TABLE vehicle
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE parking_session
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;
