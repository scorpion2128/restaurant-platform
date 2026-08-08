-- V4: Add recurring menu configuration for weekly automatic menus
-- Allows configuring a template for each day of week (Monday-Sunday)

-- Create recurring_menu_config table
CREATE TABLE recurring_menu_config (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    day_of_week INT NOT NULL CHECK (day_of_week >= 1 AND day_of_week <= 7),
    master_template_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT fk_recurring_menu_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_menu_template FOREIGN KEY (master_template_id) REFERENCES master_menu_template(id) ON DELETE CASCADE,
    CONSTRAINT uk_recurring_menu_day UNIQUE (restaurant_id, day_of_week)
);

CREATE INDEX idx_recurring_menu_restaurant ON recurring_menu_config(restaurant_id);
CREATE INDEX idx_recurring_menu_day ON recurring_menu_config(day_of_week);

-- Add is_override column to daily_menu
-- true = specific date override, false = generated from recurring config
ALTER TABLE daily_menu ADD COLUMN is_override BOOLEAN DEFAULT false;

-- Remove active column from daily_menu (no longer needed)
ALTER TABLE daily_menu DROP COLUMN IF EXISTS active;

COMMENT ON TABLE recurring_menu_config IS 'Weekly recurring menu configuration - defines which template to use for each day of week';
COMMENT ON COLUMN recurring_menu_config.day_of_week IS '1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday, 7=Sunday';
COMMENT ON COLUMN daily_menu.is_override IS 'true = specific date override, false = auto-generated from recurring config';
