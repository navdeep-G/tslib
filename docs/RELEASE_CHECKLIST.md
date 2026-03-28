# Release checklist

## Before cutting a tag

- run `./gradlew clean test jacocoTestReport`
- verify README examples still match the public API
- update `CHANGELOG.md`
- confirm version in `build.gradle` or `-PreleaseVersion=...`
- verify generated artifacts with `./gradlew publish`
- review benchmark output on representative data

## Tagging

```bash
./gradlew printReleaseCoords

git tag v0.1.0

git push origin v0.1.0
```

## Publishing

- GitHub Actions release workflow can publish to the local build repo by default
- GitHub Packages publishing uses `GITHUB_TOKEN`
- Sonatype publishing requires `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `SIGNING_KEY`, and `SIGNING_PASSWORD`

## After release

- attach release notes summarizing models, utilities, and compatibility aliases
- link benchmarks and migration notes
- create a follow-up issue for deferred work
