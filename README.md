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

## Datatrans API client

The complete client model and service formerly maintained in
[`datatrans-api-client`](https://github.com/UbiqueInnovation/datatrans-api-client)
are included in `uetlib-payment-datatrans`, alongside the payment-provider adapter.
There is no dependency on the old client artifacts. The planned first public release is:

```xml
<dependency>
    <groupId>ch.ubique.uetlib</groupId>
    <artifactId>uetlib-payment-datatrans</artifactId>
    <version>3.0.0</version>
</dependency>
```

Client types live under `ch.ubique.uetlib.payment.datatrans.client.model` and
`ch.ubique.uetlib.payment.datatrans.client.service`. For direct client use, configure
`spring.cloud.openfeign.client.config.datatrans-transactions.url`,
`ch.ubique.datatrans.merchant-id` and `ch.ubique.datatrans.password` as before.

For the payment adapter, set `ch.ubique.backend.payment.datatrans.base-url`,
`webhook-url`, `merchant-id` and `password` under that same prefix. Setting `base-url`
enables the provider; applications must supply their `RestClient` and Jackson
`ObjectMapper` beans. The adapter supplies the Feign URL and credential aliases.
Standalone client use does not create a payment provider.

The [migration command](CHANGELOG.md#migration) updates old model/service dependencies,
Java packages and class-name resources. Review the result and test your application.
Existing property names remain unchanged.

## License

Except where otherwise noted, original uetlib code is licensed under the [Apache License 2.0](LICENSE). Third-party files retain their original license terms; see [NOTICE](NOTICE) and any third-party notices in this repository.
