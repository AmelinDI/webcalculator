---
name: dev
description: Project development guidelines for this Spring Boot calculator app
---

# Project Dev Skill

## Project Context

Spring Boot 4.0.7 (Java 21) REST calculator app with static HTML frontend.

### Key Files
- `src/main/java/org/example/Main.java` — @SpringBootApplication entry point
- `src/main/java/org/example/calc/CalcController.java` — POST /api/calc, operations: +, -, *, /, %, ^
- `src/main/java/org/example/calc/CalcRequest.java` — record { double a, double b, String op }
- `src/main/java/org/example/calc/CalcResponse.java` — record { double result }
- `src/main/java/org/example/calc/ApiError.java` — record { String error }
- `src/main/java/org/example/calc/GlobalExceptionHandler.java` — IllegalArgumentException → 400
- `src/main/resources/static/index.html` — calculator UI (Russian), logout-кнопка, X-XSRF-TOKEN в fetch
- `src/main/resources/static/login.html` — login form (Russian), POST /login через fetch
- `src/main/java/org/example/security/SecurityConfig.java` — form-login (`/login.html`), сессии (JSESSIONID), `POST /logout`, CSRF через cookie `XSRF-TOKEN`, in-memory user `user`/`password` (BCrypt)
- `src/main/java/org/example/security/CsrfCookieFilter.java` — выдаёт cookie `XSRF-TOKEN` на первом запросе (без неё первый POST /login упирался бы в MissingCsrfTokenException)
- `src/test/java/org/example/security/SecurityIT.java` — 6 интеграционных тестов: редирект на /login.html без сессии, CSRF-cookie на /login.html, неудачный логин → ?error, API без auth → 302, API с сессией → 200, logout инвалидирует сессию
- `pom.xml` — spring-boot-starter-parent 4.0.7, starter-web + starter-security, тестовые starter-test + starter-webmvc-test + security-test, surefire includes `*IT`

### Architecture
```
POST /api/calc { "a": 1, "b": 2, "op": "+" } → { "result": 3 }
                    ↓
            GlobalExceptionHandler
                    ↓
        400 { "error": "..." }  (for bad op or div by zero)
```

## Development Guidelines

### Adding a New Operation
1. Add the operator symbol to the switch in `CalcController.java`
2. Update the `<select>` in `index.html` with a new `<option>`
3. Add unit test in `CalcControllerTest.java`
4. Add integration test in `CalcControllerIT.java`

### Testing Strategy
- Test stack in pom.xml: `spring-boot-starter-test`, `spring-boot-starter-webmvc-test` (MockMvc), `spring-security-test`
- Unit test: `CalcControllerTest` — test each operation with boundary values
- Integration test: `CalcControllerIT` with `@SpringBootTest` + `MockMvc` — surefire already includes `**/*IT.java`
- API tests need a logged-in session, not headers: `GET /login.html` on a `MockHttpSession` (sets `XSRF-TOKEN` cookie), then `POST /login` with `.param("username","user").param("password","password").with(csrf())`, reuse the same session for `POST /api/calc` (also `.with(csrf())`)
- Test: div by zero → 400, unknown op → 400, valid ops → 200 (with session), no auth → 302 redirect to `/login.html` (not 401 — it's form-login, not basic)
- Test: floating point edge cases (very large/small numbers)

### Spring Boot 4 / Security 7 test gotchas (learned 2026-08-15)
- `spring-boot-starter-test` no longer ships MockMvc autoconfigure: need `spring-boot-starter-webmvc-test`, `@AutoConfigureMockMvc` moved to `org.springframework.boot.webmvc.test.autoconfigure`
- spring-security-test MockMvc helpers moved to `org.springframework.security.test.web.servlet.request` (was `...test.context.servlet.request`)
- CSRF is ON for all POSTs (`/login`, `/api/calc`, `/logout`): every MockMvc POST needs `.with(csrf())` from `org.springframework.security.test.web.servlet.request`
- Lazy CSRF token (Security 6.1+/7): `CsrfFilter` doesn't generate the token on GETs, so `CsrfCookieFilter` (added after `CsrfFilter`) forces generation — without it the login page has no `XSRF-TOKEN` cookie and the first `POST /login` 403s
- Default `XorCsrfTokenRequestAttributeHandler` breaks the cookie→`X-XSRF-TOKEN` header pattern for a static frontend (it expects the XOR-ified value); `SecurityConfig` uses plain `CsrfTokenRequestAttributeHandler` instead — don't "fix" it back
- Spring Security 7 rotates the CSRF token on every successful login (`CsrfAuthenticationStrategy` sends `Set-Cookie: XSRF-TOKEN=; Expires=1970` in the login 302); `CsrfCookieFilter` re-issues a fresh cookie on the next page GET. A curl flow must GET a page and re-read the token after login (browser does it implicitly when following the redirect to `/`); the stale token 403s

### Validation Enhancements
- Add `@Valid` on `@RequestBody` with custom validator for `CalcRequest`
- Validate `op` is one of known operations (use enum)
- Consider NaN/Infinity checks on inputs
- Add request DTO with `@NotBlank` for `op`

### Possible New Features
- **History**: store recent calculations (in-memory or Redis)
- **Chaining**: POST /api/calc/chain with array of operations
- **Units**: add unit conversion endpoints
- **Logging**: add Spring Actuator for health/metrics
- **Docker**: add Dockerfile for containerized deployment
- **CI**: add GitHub Actions for build/test/deploy
- **Docs**: add OpenAPI/Swagger documentation
- **Caching**: add Redis caching for repeated calculations
- **Frontend**: migrate to React/Vue or add PWA support

### Common Commands
| Task | Command |
|------|---------|
| Compile | `mvn compile` |
| Run tests | `mvn test` |
| Package | `mvn spring-boot:run` |
| Build jar | `mvn package` |

### Spring Boot 4.0 Notes
- Uses Jakarta EE (javax → jakarta package)
- record types work out of the box for request/response
- Switch expressions are the idiomatic way for operation dispatch
- GlobalExceptionHandler with @RestControllerAdvice is the standard pattern
