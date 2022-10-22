# Contribute to Crux

Crux is a modern, robust library for parsing HTML articles. With all the content on the Web out
there, there is always lots of room for improvement. We will gladly accept your pull requests that
make parsing more accurate, or add new features & metadata detection.

To maintain the integrity of the library, we have a few simple expectations from all code submitted.

1. The expected style for code formatting is available in the repo using the
   [EditorConfig](https://editorconfig.org/) standard. We recommend using a JetBrains IDE for
   Kotlin, and configuring it to automatically use the `.editorconfig` file included in this
   repository.
1. Crux is fully unit-tested, and we want to keep it that way. All new code should include unit
   tests.
1. For parsing improvements, Crux’s rich suite of integration tests should be updated to reflect
   the parsing changes. Authors may either choose to test their improvements with existing HTML test
   files, or add new ones, as appropriate.
1. All current tests should continue to pass. Either update the tests in the same commit, or modify
   new code so that existing tests continue to pass.
1. Changes should be self-contained as far as possible. When implementing multiple independent
   improvements, each one should be in its own commit.

# Publish a New Release

## Create a New Release

1. Ensure all tests pass & CI indicates that the status is green.
1. Update `VERSION_NAME` in `gradle.properties`.
1. Create a new commit for the version number change, naming it `Bump version to x.y.z`.
1. Tag that commit as `vx.y.z` (must match `v[0-9]+.[0-9]+.[0-9]+`).
1. Push all commits & tags to GitHub.

## Publish to Maven Central

Confirm that the `gradle.properties` file in the home directory (`~/.gradle/gradle.properties`) is
present and [set up correctly](#set-up-key-signing-on-a-new-machine).

  ```shell
  ./gradlew publish
  ```
## Close & Release Manually

### Manually

Assuming `./gradlew publish` has been run after a new version has been released.

- Go to https://oss.sonatype.org/#stagingRepositories, login as `chimbori`.
- Select the `comchimboricrux-xxxx` repo
- Click on `Close` from the top toolbar, wait for it to complete.
- Click on `Release` from the top toolbar.

## Set Up Key Signing on a New Machine

### Signing

1. Install GPG, e.g. `brew install gpg` on macOS.
1. Locate stored credentials from private storage.
1. Run `restore-keys.sh` from the stored credentials directory.
1. Enter the password for `chimbori` when prompted. This password is different from the Sonatype/Nexus password.

### Credentials

1. Copy `gradle.properties.sample` to `~/.gradle/gradle.properties` and fill in the missing redacted credentials.
1. If `gradle.properties.private` exists, it may be used instead. `gradle.properties.private` is configured to be
   `.gitignore`d, so make sure it is never pushed to a public repo.
1. The new machine is now ready and configured for pushing to Maven Central.
