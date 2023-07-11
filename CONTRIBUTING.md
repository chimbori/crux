# Contribute to Crux

Crux is a modern, robust library for parsing HTML articles. With all the content on the Web out
there, there is always lots of room for improvement. We will gladly accept your pull requests that
make parsing more accurate, or add new features & metadata detection.

To maintain the integrity of the library, we have a few simple expectations from all code submitted.

1. Before sending a pull request, please open an issue to discuss your changes. Maintainers
   will offer feedback and help validate your idea as well as overall design before you spend any
   time writing code.
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
