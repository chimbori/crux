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
