ALTER TABLE payments
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

UPDATE payments
SET expires_at = CURRENT_TIMESTAMP + INTERVAL '2 days'
WHERE expires_at IS NULL;

ALTER TABLE payments
    ALTER COLUMN expires_at SET NOT NULL;

ALTER TABLE payment_attempt
    ADD COLUMN payment_method VARCHAR(30);

UPDATE payment_attempt attempt
SET payment_method = payment.payment_method
FROM payments payment
WHERE attempt.payment_id = payment.id
  AND attempt.payment_method IS NULL;

ALTER TABLE payment_attempt
    ALTER COLUMN payment_method SET NOT NULL;
