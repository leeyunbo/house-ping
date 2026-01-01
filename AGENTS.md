# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/yunbok/houseping`: Hexagonal layout dividing `domain` (model, service, port) from `infrastructure` (config, adapter, dto, persistence). Keep adapters thin and let domain objects drive behavior.
- `src/main/resources`: Spring configuration (profiles, scheduler cron) and templates; `data/` holds sample payloads and `logs/` captures runtime output for inspection only.
- `src/test/java`: Mirrors the `main` package tree; add fixtures beside the code they exercise. Shared stubs or Testcontainers setup belong under `src/test/java/.../support`.
- `script/` hosts helper shell scripts, while Gradle metadata lives under `gradle/`; avoid editing generated wrappers unless toolchain upgrades demand it.

## Build, Test, and Development Commands
```bash
./gradlew bootRun --args='--spring.profiles.active=local'   # start the API with local profile
./gradlew build                                            # compile, run unit tests, assemble fat JAR
java -jar build/libs/houseping-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
./gradlew test                                             # JUnit 5 unit and slice tests
./gradlew jacocoTestReport                                 # HTML coverage report in build/reports/jacoco
```
Run commands from the repo root; Gradle handles Java 21 via the configured toolchain.

## Coding Style & Naming Conventions
- Use 4-space indentation, Lombok where already adopted, and favor constructor injection for Spring components.
- Package names stay lowercase, class names use `UpperCamelCase`, and methods/fields use `lowerCamelCase`.
- Ports describe business verbs (`AlarmSenderPort`), adapters describe technology (`TelegramAdapter`), and DTOs end with `Request`/`Response`.
- Keep controllers slim, delegate logic to domain services, and guard external calls with resilience components present in `infrastructure`.

## Testing Guidelines
- JUnit 5, Mockito, and Spring Boot Test are available; prefer focused tests in `domain` packages and use `@SpringBootTest` only for genuine integration paths.
- Match test class names to the subject class (e.g., `SubscriptionServiceTest`) and use `given_when_then` method naming for clarity.
- Failing branches require at least unit coverage; add integration tests when touching adapters or persistence. Generate coverage with JaCoCo before opening a PR.

## Commit & Pull Request Guidelines
- Branch names follow `feature/<topic>` or `fix/<topic>`. Commit messages start with an imperative verb and stay under 72 characters (`Add LH adapter retry`).
- Each PR must link related issues, describe observable changes, list test commands run, and include screenshots/log snippets for user-visible updates.
- Rebase onto the latest `main` before requesting review, ensure CI (Gradle build + tests) passes, and call out new configs (e.g., Telegram tokens) in the PR body.

## Security & Configuration Tips
- Never commit secrets; provide sample entries via `application-*.yml.example` files or document required environment variables in README updates.
- Local profile should point to the in-memory H2 database; production profiles must pull credentials from environment variables or the hosting secret store.
- Scrub personally identifiable data before sharing captured payloads in `data/` or attaching log files to issues.
