# CLAUDE.md

Guidance for Claude Code (and other coding agents) working in this repository.

## Repo Overview

This is `triathematician/tornadofx` — a fork of
[edvin/tornadofx (`jdk10` branch)](https://github.com/edvin/tornadofx/tree/jdk10)
updated to build against Java 17, JavaFX 21 (currently `21.0.12`), and Kotlin 2.4.x+
(currently `2.4.10`) — see the `<properties>` block in `pom.xml` for the exact pinned
versions. TornadoFX is a lightweight JavaFX framework for Kotlin.

Active development happens on the `jfx17-fx21-kotlin21` branch (the name predates the
Kotlin range above — Kotlin has since been bumped forward within that branch).

- Single Maven module — one `pom.xml` at the repo root, artifact
  `com.googlecode.blaisemath.tornado:tornadofx-fx21k2`. No multi-module/reactor setup.
- Source lives under `src/main/kotlin`, tests under `src/test/kotlin`.
- CI runs via `.github/workflows/run-tests.yml` on every push/PR: JDK 17 on `ubuntu-latest`,
  `mvn test` wrapped in `xvfb-run` (JavaFX/TestFX needs a virtual display on Linux).

## Build & Test

```bash
mvn install
mvn test
```

Requires **JDK 17** and **Maven >= 3.6.3** (enforced by `maven-enforcer-plugin`).

Tests run through TestFX and JavaFX, so they need a display/headless-capable environment.
Surefire is configured with extra `--add-exports`/`--add-opens`/`--add-reads` JVM args
(see `<argLine>` in `pom.xml`) required for reflection into JavaFX/Kotlin internals — don't
strip these if invoking surefire directly.

## Release Process

Released via `maven-release-plugin` + `central-publishing-maven-plugin` (Sonatype Central):

```bash
mvn release:prepare
mvn release:perform
```

This pushes commits/tags and publishes irreversibly to Maven Central — an agent should
**never** run these commands without explicit user confirmation immediately beforehand,
and by default should only *prepare* a release checklist and let the user run the commands.

After `mvn release:perform` completes, the artifacts land in a Central Publishing Portal
deployment that still needs a manual publish action: go to
**https://central.sonatype.com/publishing** and hit **Publish** for that deployment.

## Dependency Maintenance Playbook

Use this generic, repeatable process for any dependency or tooling version bump
(Kotlin, JavaFX, FontAwesomeFX, plugin versions, etc.) — not a one-off recipe for any
single library. Relevant version properties are centralized at the bottom of `pom.xml`
(`kotlin.version`, `javafx.version`, `de.jensd.fontawesomefx.version`, the `maven.*.version`
plugin versions, etc.).

**Base branch note:** active development happens on `jfx17-fx21-kotlin21`, but this repo's
GitHub-registered default branch is still `master`. Branch from and target
`jfx17-fx21-kotlin21` throughout this playbook — always pass `--base jfx17-fx21-kotlin21`
explicitly to `gh pr create` (and any other `gh`/`git` command that assumes a default
branch), since a plain invocation would otherwise target `master`.

1. **Open a tracking issue** — `gh issue create` in `triathematician/tornadofx`, e.g. title
   "Dependency maintenance: <date/scope>", with a checklist body that gets filled in as the
   process progresses.
2. **Survey dependencies**:
   ```bash
   mvn versions:display-dependency-updates
   mvn versions:display-plugin-updates
   ```
   Also check `gh` for open Dependabot PRs/alerts on the repo. Compile one combined list of
   available updates.
3. **Classify updates** — patch/minor (batchable, low risk) vs. major (review changelogs/
   breaking changes individually, especially Kotlin and JavaFX major bumps). Record the
   classification in the tracking issue.
4. **Decide scope for this cycle** — the agent proposes which updates to tackle now (based
   on the classification in step 3) and which to defer, but the **user approves the final
   list** before any code changes are made.
5. **Branch and update** — one branch, cut from `jfx17-fx21-kotlin21`, touching `pom.xml`
   version properties (and README where a version is user-visible, e.g. the
   `jvmTarget`/Kotlin-version notes).
6. **Run automated tests**:
   ```bash
   mvn install
   mvn test
   ```
7. **Manual smoke test, conditionally** — only for major-version or otherwise
   behavior-relevant bumps (e.g. a JavaFX or Kotlin major/minor bump), exercise a sample
   TornadoFX app manually to confirm rendering/reflection still works. State the reasoning
   for running or skipping this step. The manually-runnable apps under
   `src/test/kotlin/tornadofx/testapps` are a candidate for this (not wired into the
   automated test run) — usefulness unverified, so sanity-check before relying on them.
8. **Fix and iterate** until all triggered tests are green.
9. **Push the branch and open a PR** via `gh pr create --base jfx17-fx21-kotlin21`,
   referencing the tracking issue, summarizing the change and test evidence gathered.
10. **Wait for review, then the user merges.** The agent does not merge dependency-update
    PRs itself — a human must review and click merge. Address any review feedback by
    pushing fixes to the same branch.
11. **On merge** — close the tracking issue (`gh issue close`), pull `jfx17-fx21-kotlin21`
    locally.
12. **Release — prepared by the agent, executed by the user.** Prepare a release
    checklist/notes but do **not** run `mvn release:prepare`/`mvn release:perform` — that's
    a manual step the user runs, since it publishes irreversibly to Maven Central.
13. **Publish on Sonatype Central.** After `mvn release:perform` completes, go to
    **https://central.sonatype.com/publishing** and hit **Publish** for that deployment.
