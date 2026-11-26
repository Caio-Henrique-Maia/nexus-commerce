ALTER TABLE payments
    ADD CONSTRAINT uk_payment_order UNIQUE (order_id);

ALTER TABLE products
    ADD CONSTRAINT ck_products_stock_non_negative
        CHECK (stock_quantity >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT ck_order_items_quantity_positive
        CHECK (quantity > 0);

ALTER TABLE order_items
    ADD CONSTRAINT ck_order_items_unit_price_non_negative
        CHECK (unit_price > 0);
