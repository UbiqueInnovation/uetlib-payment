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

## PostFinance environment selection

`uetlib-payment-postfinance` supports the PostFinance transaction environment selection strategy
through the following Spring Boot property:

```properties
ch.ubique.backend.payment.postfinance.environment-selection-strategy=FORCE_TEST
```

Supported values are `FORCE_TEST`, `FORCE_PRODUCTION`, and
`USE_CONFIGURATION`. The default is `USE_CONFIGURATION` for backward compatibility. Applications
should force the expected environment when test and production payments must not be switchable in
the PostFinance dashboard. Programmatic configuration uses the uetlib-owned
`PostfinanceEnvironmentSelection` enum, so applications do not depend on PostFinance SDK
types.

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
