# Changelog

## 3.0.0 (unreleased)

- Rename uetlib configuration keys and property references to `ch.uetlib.*`, including Spring bindings, `@Value`,
  conditions, scheduling expressions and configuration files. Migrate external deployment configuration as well.

- Move Java packages and class references from `ch.ubique.uetlib` to `ch.uetlib`.

### Breaking changes

- Inline the Datatrans API client model and service into `uetlib-payment-datatrans`. Replace the old
  `ch.ubique.datatrans:datatrans-api-client-model` / `datatrans-api-client-service` dependencies with
  `ch.uetlib:uetlib-payment-datatrans:3.0.0`; packages move to `ch.uetlib.payment.datatrans.client`. The migration
  recipe handles these changes. Existing client credentials and provider configuration keys remain unchanged.

- Move Java packages from `ch.ubique.backend` to `ch.uetlib`, including class-name resources. The combined OpenRewrite
  migration also renames known Spring configuration keys to `ch.uetlib.*`.
- Remove the inherited Ubique Artifactory repository and deployment destination; private consumers must configure their
  own registry.
- Add Apache-2.0 licensing for original code, preserve third-party licenses, and add public project metadata and
  contribution/security guidance.

- Move Maven coordinates from `ch.ubique:uetlib-*` to `ch.uetlib:uetlib-*`.
- Align uetlib parent, BOM and library references with the migration releases.

### Migration

After the migration releases and `uetlib-rewrite` are published, run this from the consuming project, then review the
diff and run its tests:

```sh
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run \
  -Drewrite.recipeArtifactCoordinates=ch.uetlib:uetlib-rewrite:RELEASE \
  -Drewrite.activeRecipes=ch.uetlib.MigrateToPublicUetlib
```

The recipe updates Maven coordinates/versions, Java references and known class-name resources. Review application-owned
packages under the old namespace, Kotlin, reflection patterns, native-access module flags and persisted class names
separately. Use `:dryRun` instead of `:run` to preview a patch.

## 2.0.8 - 2026-08-25

### Fixed

- Treat PostFinance webhook header names case-insensitively when validating signatures.

## 2.0.7 - 2026-08-24

- latest uetlib parent
- version updates

## 2.0.5 - 2026-08-24

### Added

- Allow applications to force the PostFinance test or production transaction environment.

## 2.0.4 - 2026-08-07

### Improved

- Validate PostFinance auth key directly on config properties with a new annotation. Gives nicer errors on startup,
  like:
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
