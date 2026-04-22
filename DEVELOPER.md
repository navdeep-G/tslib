# Developer Guide

This document covers everything needed to contribute to tslib: local setup, project structure, coding conventions, how to add a model or endpoint, and the PR process.

---

## Prerequisites

- **Java 17** — the toolchain is pinned to 17 in `build.gradle`; Java 21 works for running but the CI matrix targets 17
- **Gradle** — use the wrapper (`./gradlew`); do not install Gradle globally, version mismatches cause subtle build failures
- No other tooling is required; all dependencies download on first build

---

## Modules

The repo is a Gradle multi-project build with two modules:

| Module | Directory | Purpose |
|---|---|---|
| `tslib` (root) | `/` | The library — published to Maven Central, no web dependencies |
| `tslib-api` | `tslib-api/` | Spring Boot REST API wrapping the library |

Keep them independent. The library must never import Spring. The API module depends on the library via `project(':')`.

---

## Building

```bash
# Run all library tests
./gradlew test

# Full verification (tests + coverage + Javadoc)
./gradlew clean test jacocoTestReport javadoc

# Build the REST API fat JAR
./gradlew :tslib-api:bootJar

# Run the REST API locally (hot-reload)
./gradlew :tslib-api:bootRun

# Compile examples
./gradlew compileExamples
./gradlew runExamples

# Publish to local Maven cache (useful before depending on a local build)
./gradlew publishToMavenLocal
```

Reports land in:
- `build/reports/tests/test/` — test results
- `build/reports/jacoco/test/html/` — coverage
- `build/docs/javadoc/` — API docs

---

## Project structure

```
tslib/
├── src/main/java/tslib/
│   ├── collect/          Collect — exploratory analysis wrapper
│   ├── dataquality/      MissingValueImputer, OutlierDetector, Winsorizer
│   ├── decomposition/    STLDecomposition
│   ├── diagnostics/      LjungBoxTest
│   ├── evaluation/       Metrics, backtest, train/test split, ModelBenchmark
│   ├── math/             Probability (internal)
│   ├── model/
│   │   ├── arima/        ARIMA, SARIMA, ARIMAX, VARModel, ArimaOrderSearch
│   │   ├── expsmoothing/ SingleExpSmoothing, DoubleExpSmoothing, TripleExpSmoothing
│   │   └── statespace/   KalmanFilter, LocalLevelModel
│   ├── movingaverage/    Simple, Exponential, Weighted, Cumulative
│   ├── selection/        AutoArima, AutoETS
│   ├── tests/            AugmentedDickeyFuller, KPSSTest
│   ├── transform/        Transform, Differencing
│   └── util/             Stats, LinearAlgebra, ModelSerializer, Util
├── src/test/java/tslib/  Mirrors the main package tree
├── examples/             Standalone runnable Java examples
│   └── api/              curl, Python, and R client examples
├── config/checkstyle/    checkstyle.xml
├── docs/                 MODEL_SELECTION_GUIDE, RELEASE_CHECKLIST, etc.
└── tslib-api/            Spring Boot REST API subproject
    └── src/main/java/tslib/api/
        ├── controller/   One controller per functional group
        ├── dto/          Request and response POJOs
        ├── config/       OpenAPI config
        └── exception/    GlobalExceptionHandler, ErrorResponse
```

---

## Coding conventions

### General

- Target Java 17. Use `var`, switch expressions, records, and text blocks freely — all are available.
- No comments that describe *what* the code does. Only add a comment when the *why* is non-obvious: a subtle invariant, a workaround for a known edge case, or a constraint that would surprise a reader.
- No docstrings on methods that are self-explanatory from the name and signature. Package-level `package-info.java` files carry module-level intent where needed.

### Library (`tslib`)

- **No web or framework dependencies.** The only production dependency is `commons-math3`. Keep it that way.
- **Every public model class must implement `java.io.Serializable`** and declare `serialVersionUID`.
- **All models follow the fluent fit pattern**: `fit()` returns `this` (or the implementation type). This enables `new Model(params).fit(data).forecast(steps)`.
- **Unchecked exceptions only.** Throw `IllegalArgumentException` for bad input, `IllegalStateException` for calls made before `fit()`. Do not declare checked exceptions on public methods.
- **Defensive copies on all public getters** that return mutable collections. Return `Collections.unmodifiableList(...)` or a copy, never the internal array/list directly.
- **Thread safety**: model instances are not thread-safe (fit mutates state). Callers that share models across threads must synchronize externally. Document this if it is non-obvious.
- **Numerical helpers** go in `tslib.util.LinearAlgebra` or `tslib.util.Stats`, not scattered across model classes.

### REST API (`tslib-api`)

- **One controller per functional group**, mapped under `/api/<group>/`. Do not create god controllers.
- **All endpoints are `POST` with a JSON body.** Time-series data is too large and structured for query parameters.
- **Every request DTO must carry `@Valid` at the controller parameter** and use Bean Validation annotations (`@NotEmpty`, `@Min`, `@DecimalMin`/`@DecimalMax`) on fields. Never hand-validate in the controller if a constraint annotation covers it.
- **Response DTOs are plain POJOs** with getters/setters — no Jackson annotations unless a field name must differ from the Java name. Keep serialization implicit.
- **No business logic in controllers.** A controller method constructs the library object, calls it, maps the result to a DTO, and returns. If a mapping gets complex, extract a private helper method.
- **Error handling belongs in `GlobalExceptionHandler`**, not in individual controller methods. Controllers should let library exceptions propagate.

### Testing

- **JUnit 5** throughout. Use `@Test`, `@ParameterizedTest`, `@TempDir`, and `@BeforeEach`/`@AfterEach`. No JUnit 4 annotations.
- **70% line coverage is enforced by JaCoCo** and will fail the build if breached. New code must be accompanied by tests that bring coverage up, not just to the threshold.
- Tests live in `src/test/java/tslib/` mirroring the main package tree. A class `tslib.model.arima.ARIMA` has its tests in `tslib.model.arima.ARIMATest`.
- **Test real numerical behavior**, not just that no exception is thrown. Assert on specific output values (with appropriate delta) for at least the happy path and one edge case.
- **No mocking of library internals.** If a test needs a fitted model, construct and fit one with real data. Mock only external I/O (files, sockets) when unavoidable.

---

## Adding a new model to the library

1. **Create the implementation** in the appropriate sub-package (e.g. `tslib.model.arima.` for a new ARIMA variant).
   - Implement `tslib.model.TimeSeriesModel` (or `tslib.model.expsmoothing.ExponentialSmoothing` for smoothing models).
   - Declare `private static final long serialVersionUID = 1L;`.
   - Follow the fluent `fit()` pattern.

2. **Add a compatibility alias** in `tslib.model` if the model belongs to a family that already has top-level aliases (e.g. `tslib.model.MyModel extends tslib.model.mypackage.MyModel`).

3. **Write tests** in the mirrored test package. Cover at minimum:
   - fit and forecast on a short synthetic series
   - `forecastWithIntervals` if the model supports it
   - parameter validation (negative orders, empty data, etc.)

4. **Add an example** in `examples/` following the pattern of the existing files.

5. **Update `README.md`** — add the new class to the package-map and phase-additions sections.

6. **Update `CHANGELOG.md`** — add an entry under `## Unreleased`.

---

## Adding a new REST endpoint

1. **Add or update the request DTO** in `tslib-api/src/main/java/tslib/api/dto/`. Use Bean Validation annotations for all constraints. Provide sensible field defaults for optional parameters.

2. **Add or update the response DTO** in the same package. Keep it flat where possible; nest only when the structure genuinely demands it.

3. **Add the endpoint** to the appropriate controller in `tslib-api/src/main/java/tslib/api/controller/`. Add `@Operation(summary = "...")` for Swagger. Keep the method body short — construction, call, map, return.

4. **Verify the global exception handler covers the error cases** the new library call can throw. If a new exception type is possible, add a handler in `GlobalExceptionHandler`.

5. **Add a curl example** in `examples/api/curl_examples.sh`, a Python call in `examples/api/python_client.py`, and an R call in `examples/api/r_client.R`.

6. **Smoke-test manually**: `./gradlew :tslib-api:bootRun`, then hit the endpoint with curl or the Swagger UI at `http://localhost:8080/swagger-ui`.

---

## Pull request checklist

Before opening a PR, confirm:

- [ ] `./gradlew clean test jacocoTestReport` passes with no failures and coverage ≥ 70%
- [ ] `./gradlew checkstyleMain` produces no new violations (warnings are non-blocking but should be addressed)
- [ ] New public library classes implement `Serializable` and have `serialVersionUID`
- [ ] New request DTOs have `@Valid` annotations on all constrained fields
- [ ] `CHANGELOG.md` has an entry under `## Unreleased`
- [ ] `README.md` is updated if a new class is added
- [ ] No new production dependencies added to the library without discussion in the PR description
- [ ] PR description includes: what changed, why, and any compatibility notes

---

## Code quality tools

All three run as part of `./gradlew build` but are non-blocking (`ignoreFailures = true`). Reports are in `build/reports/`.

| Tool | Config | Report |
|---|---|---|
| Checkstyle | `config/checkstyle/checkstyle.xml` | `build/reports/checkstyle/` |
| SpotBugs | Default ruleset | `build/reports/spotbugs/` |
| JaCoCo | 70% line coverage minimum | `build/reports/jacoco/` |

JaCoCo is the only one that can actually fail the build (via `jacocoTestCoverageVerification`). The other two emit warnings. Treat SpotBugs `HIGH` findings as bugs; `MEDIUM` and below are advisory.

---

## Release process

See `docs/RELEASE_CHECKLIST.md` for the full flow. The short version:

1. Update `CHANGELOG.md` — move `## Unreleased` entries under a new version heading.
2. Tag: `git tag v0.x.y && git push origin v0.x.y`.
3. CI picks up the tag and runs the release workflow (`.github/workflows/release.yml`), which signs and publishes to Maven Central via OSSRH.
4. For the REST API, build the fat JAR (`./gradlew :tslib-api:bootJar`) and publish the Docker image separately.

Snapshot builds (version ending in `-SNAPSHOT`) publish automatically to the OSSRH snapshot repository on every push to `master`.
