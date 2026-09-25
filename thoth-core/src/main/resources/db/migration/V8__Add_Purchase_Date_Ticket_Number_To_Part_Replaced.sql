-- V8: Add purchase_date and ticket_number to part_replaced table
-- Allows tracking when the replacement part was purchased and its ticket/invoice number
ALTER TABLE part_replaced ADD COLUMN IF NOT EXISTS purchase_date DATE;
ALTER TABLE part_replaced ADD COLUMN IF NOT EXISTS ticket_number VARCHAR(100);
