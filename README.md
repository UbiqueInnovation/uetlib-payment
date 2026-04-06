# uetlib-payment

Reusable backend payment abstractions and provider adapters for the `uetlib` ecosystem.

## Modules

- `uetlib-payment`
  - Provider-neutral payment contract, commands, result types, and shared enums.
- `uetlib-payment-datatrans`
  - Datatrans implementation of the shared payment contract.
- `uetlib-payment-postfinance`
  - PostFinance implementation of the shared payment contract.

## Scope

The base module is intentionally provider-neutral. Provider-specific protocol logic lives in dedicated adapter modules.

The shared API currently covers:

- payment initialization
- status lookup
- webhook processing
- capture / cancel operations
- shared payment lifecycle states and payment methods

## Capture Modes

`PaymentCaptureMode` defines how a payment should be completed:

- `AUTOMATIC`
  - The provider completes or captures the payment during the normal checkout flow.
- `MANUAL`
  - The provider only authorizes first, and the backend completes or captures later.
