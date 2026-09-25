-- V7: Make serial_number nullable
-- Previously required, now optional to allow equipment creation without serial number
ALTER TABLE equipment ALTER COLUMN serial_number DROP NOT NULL;
