ALTER TABLE payments
    ADD CONSTRAINT uk_payment_order UNIQUE (order_id);