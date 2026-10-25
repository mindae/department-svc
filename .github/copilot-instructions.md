
# Department Service — Copilot Instructions

## Project context
- This is an independent Spring Boot microservice.
- It is part of a learning platform with employee-svc,
  api-gateway and discovery-svc.
- Spring Data JPA and H2 provide local persistence.
- Eureka provides service discovery.

## Coding conventions
- Follow existing Java and Spring Boot project conventions.
- Prefer constructor injection.
- Keep controllers thin and business logic in services.
- Use repositories for persistence operations.
- Reuse existing DTOs and classes where appropriate.
- Avoid introducing dependencies without justification.

## API compatibility
- Preserve existing REST response contracts.
- GET /api/v1/departments currently returns one
  department object for compatibility.
- GET /api/v1/departments/all returns a collection.
- Do not change existing endpoint behavior without
  explicitly explaining the compatibility impact.

## Testing
- Use JUnit 5 and Mockito for unit tests.
- Use Spring integration tests when application context,
  JPA or HTTP behavior requires them.
- Include happy-path and meaningful failure cases.
- Do not claim tests passed unless they were executed.

## Scope and safety
- Modify only files relevant to the requested task.
- Explain significant architecture or behavior changes.
- Avoid committing generated H2 database files.
- Never introduce hardcoded credentials or secrets.
- Do not run destructive commands or commit/push
  without explicit authorization.
- Review affected tests and report any unverified behavior.
