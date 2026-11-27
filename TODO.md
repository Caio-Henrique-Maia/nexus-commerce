# TODO

## Order and payment flow

- [ ] Create a customer and at least two products with positive prices and stock.
- [ ] Create an order with multiple products and verify that the total is the sum of all item subtotals.
- [ ] Send the same product more than once in the order and verify that quantities are aggregated.
- [ ] Try creating an order with an empty item list, a missing product, zero/negative quantity, or insufficient stock.
- [ ] Retrieve the order and verify that its items and historical unit prices are returned.
- [ ] Update the customer of an order using `PATCH /api/orders/{id}`; confirm that item details and total are unchanged.
- [ ] Create a payment using `POST /api/payments` with an order ID and payment method; verify that the amount comes from the order total.
- [ ] Try creating a second payment for the same order and verify that the API rejects it.
- [ ] Process the payment using `POST /api/payments/process` with the order ID.
- [ ] Repeat payment processing after a simulated timeout and verify that the same idempotency key is reused.
- [ ] Verify that a confirmed payment cannot result in a second gateway charge.

## Follow-up engineering work

- [ ] Add automated tests for order total calculation, nested DTO validation, payment creation, and duplicate payment prevention.
- [ ] Make stock reservation/decrement atomic to prevent overselling under concurrent order creation.
- [ ] Define order cancellation and stock release rules.
- [ ] Review whether order update should be restricted after payment processing starts.
- [ ] Add authentication and derive the customer identity from the authenticated principal instead of accepting customerId from the request.
- [ ] Introduce service boundaries and extract the payment module into an independent service only after the modular-monolith flow is stable.
