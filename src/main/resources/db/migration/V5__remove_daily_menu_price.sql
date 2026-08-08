-- Daily menus now derive their item prices from the restaurant products.
-- The recurring/override API no longer accepts or persists a menu-level price.
ALTER TABLE daily_menu DROP COLUMN IF EXISTS menu_price;
