# Contributing to Crux

Crux is a modern, robust library for parsing HTML articles. With all the content on the Web out
there, there is always lots of room for improvement. We will gladly accept your pull requests that
make parsing more accurate, or add new features & metadata detection.

To maintain the integrity of the library, we have a few simple expectations from all code submitted.

1. Crux follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
All new code must adhere to this guide. Please note rules around indentation (2 spaces, no tabs)
and that braces are required around single-line clauses, e.g.:

    ```java
        if (condition) {
          x = 0;  // Braces are required around this line.
        }
    ```

1. Crux is fully unit-tested, and we want to keep it that way. All new code should include unit
   tests.
1. For parsing improvements, Crux’s rich suite of integration tests should be updated to reflect
   the parsing changes. Authors may either choose to test their improvements within existing HTML
   test files, or add new ones, as appropriate.
1. All current tests should continue to pass. Either update the tests in the same CL, or modify the
   new code, so that existing tests continue to pass.
1. Changes should be self-contained, as far as possible. When implementing multiple independent
   improvements, each one should be in its own pull request.

# Notes for Maintainers

## Pushing a new release to Maven

1. Change version number in `build.gradle`.
2. Run `./gradlew uploadArchives`.
3. Go to https://oss.sonatype.org/#stagingRepositories
4. Select the comchimboricrux-xxxx repo, then click on “Close” from the toolbar.