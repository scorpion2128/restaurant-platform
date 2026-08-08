-- V3: Remove price_override column from daily_menu_item table
-- This field is no longer needed as we'll use the original product prices only

ALTER TABLE daily_menu_item DROP COLUMN IF EXISTS price_override;
