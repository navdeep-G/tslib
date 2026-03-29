# Testing guide

This project now treats testability as a release-blocking concern.

## Recommended local verification

```bash
./gradlew clean test jacocoTestReport javadoc
```

## What to check before merging

- short-series behavior for forecasting models
- missing-value handling at the start and end of a series
- seasonal edge cases with limited history
- ARIMAX regressor alignment and mismatch failures
- prediction-interval width growth for multi-step forecasts
- benchmark report generation for representative comparisons

## CI expectations

- pull requests should pass on Java 17 and Java 21
- nightly verification should catch environment drift
- release tags should run the full build and publishing pipeline

## Reports

- unit test report: `build/reports/tests/test/index.html`
- coverage report: `build/reports/jacoco/test/html/index.html`
- generated Javadocs: `build/docs/javadoc/index.html`
