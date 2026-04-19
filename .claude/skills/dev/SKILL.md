---
name: dev
description: Project development guidelines for this Spring Boot calculator app
---

# Project Dev Skill

## Project Context

Spring Boot 4.0.5 (Java 21) REST calculator app with static HTML frontend.

### Key Files
- `src/main/java/org/example/Main.java` — @SpringBootApplication entry point
- `src/main/java/org/example/calc/CalcController.java` — POST /api/calc, operations: +, -, *, /, %, ^
- `src/main/java/org/example/calc/CalcRequest.java` — record { double a, double b, String op }
- `src/main/java/org/example/calc/CalcResponse.java` — record { double result }
- `src/main/java/org/example/calc/ApiError.java` — record { String error }
- `src/main/java/org/example/calc/GlobalExceptionHandler.java` — IllegalArgumentException → 400
- `src/main/resources/static/index.html` — Static frontend (Russian UI)
- `pom.xml` — spring-boot-starter-parent 4.0.5, spring-boot-starter-web only

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
- Use `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ) — add to pom.xml
- Unit test: `CalcControllerTest` — test each operation with boundary values
- Integration test: `CalcControllerIT` with `@SpringBootTest` + `MockMvc`
- Test: div by zero → 400, unknown op → 400, valid ops → 200
- Test: floating point edge cases (very large/small numbers)

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
- **Auth**: add basic API key authentication
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
