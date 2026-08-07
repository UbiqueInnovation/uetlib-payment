# Changelog

## 2.0.4 - 2026-08-07

### Improved

- Validate PostFinance auth key directly on config properties with a new annotation. Gives nicer errors on startup, like:
  ```
  Binding to target ch.ubique.backend.payment.postfinance.PostfinancePaymentProviderProperties failed:
  
  Property: ch.ubique.backend.payment.postfinance.authKey
  Value: "OuXBRclhF5Ab0E44gBb+A="
  Origin: class path resource [application.properties] - 102:48
  Reason: Expected a Base64-encoded key with at least 256 bits for HS256 signing.
  ```

## 2.0.3 - 2026-08-04

### Fixed

- Use only the PostFinance Lightbox URL when initializing a transaction.

## 2.0.1

- updated actions to not run tests twice for sonar
- update uetlib version

## 2.0.0 - 2026-07-22

- Upgrade Spring Boot 4 / Jackson 3
- Update to `datatrans-api-client:1.0.0`
- extend `uetlib-parent` and align with uetlib conventions
- null-marked

## 1.0.3 - 2026-07-10

### Added

- Add opt-in, redacted full HTTP request and response logging for PostFinance.

## 1.0.2 - 2026-07-10

### Changed

- Upgrade the PostFinance Java SDK to 9.5.0.

## 1.0.1 - 2026-07-07

### Changed

- Upgrade the PostFinance Java SDK to 9.4.0.

## 1.0.0 - 2026-04-10

### Added

- Initial standalone payment abstractions and provider modules.
