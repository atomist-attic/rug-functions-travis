# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

[Unreleased]: https://github.com/atomist/rug-functions-travis/compare/0.18.0...HEAD

## [0.18.0] - 2017-05-30

[0.18.0]: https://github.com/atomist/rug-functions-travis/compare/0.17.1...0.18.0

Unification release

### Changed

-   Brought build restart API in line with other functions
-   Changed name of travis-restart-build to agree with other functions

## [0.17.2] - 2017-05-28

[0.17.2]: https://github.com/atomist/rug-functions-travis/compare/0.17.1...0.17.2

Silent release

### Changed

-   Remove logging

## [0.17.1] - 2017-05-26

[0.17.1]: https://github.com/atomist/rug-functions-travis/compare/0.17.0...0.17.1

Explicit release

### Changed

-   Be more explicit about types in postAuthGitHub, add logging

## [0.17.0] - 2017-05-26

[0.17.0]: https://github.com/atomist/rug-functions-travis/compare/0.16.0...0.17.0

No number release

### Changed

-   Restart build function build identifier is now a string

## [0.16.0] - 2017-05-25

[0.16.0]: https://github.com/atomist/rug-functions-travis/compare/0.15.0...0.16.0

No org release

### Changed

-   Determine Travis CI endpoint to use by querying GitHub for repo visibility

## [0.15.0] - 2017-05-02

[0.15.0]: https://github.com/atomist/rug-functions-travis/compare/0.14.1...0.15.0

Retry release

### Changed

-   Updated rug dependency to 1.0.0-m.2
-   Silence logging in tests

### Added

-   Retry to all Travis API calls
-   More exception logging

### Fixed

-   Scopes required by GitHub token

## [0.14.1] - 2017-05-02

[0.14.1]: https://github.com/atomist/rug-functions-travis/compare/0.14.0...0.14.1

### Changed

-   Retry of repo get in `getRepoRetryingWithSync`

## [0.14.0] - 2017-05-01

[0.14.0]: https://github.com/atomist/rug-functions-travis/compare/0.13.0...0.14.0

### Added

-   Exception logging to figure out why enable-repo tends to fail

## [0.13.0] - 2017-04-20

[0.13.0]: https://github.com/atomist/rug-functions-travis/compare/0.12.0...0.13.0

Disable release

### Fixed

-   Unhide `travis-disable-repo` [#4][4]

[4]: https://github.com/atomist/rug-functions-travis/issues/4

### Changed

-   Make more testable, add tests
-   Build improvements
-   Changes required for move to atomist GitHub org

## [0.12.0] - 2017-04-14

[0.12.0]: https://github.com/atomist/rug-functions-travis/compare/0.11.0...0.12.0

Public release

### Changed

-   Do not authenticate to fetch public key from public repo

## [0.11.0] - 2017-04-14

[0.11.0]: https://github.com/atomist/rug-functions-travis/compare/0.10.0...0.11.0

Scopes release

### Fixed

-   GitHub token scope

## [0.8.0] - 2017-04-12

[0.8.0]: https://github.com/atomist/rug-functions-travis/compare/0.7.0...0.8.0

Enable and encrypt release

### Added

-   New Rug functions: travis-encrypt, travis-enable-repo, travis-disable-repo

### Changed

-   Updated to rug 0.25.1

## [0.7.0] - 2017-04-11

[0.7.0]: https://github.com/atomist/rug-functions-travis/compare/0.6.1...0.7.0

Papa's got a brand new Rug release

### Changed

-   Update to rug 0.25.0

## [0.6.1] - 2017-03-30

[0.6.1]: https://github.com/atomist/rug-functions-travis/compare/0.6.0...0.6.1

Secret release

### Fixed

-   Send proper headers when starting build

## [0.6.0] - 2017-03-30

[0.6.0]: https://github.com/atomist/rug-functions-travis/compare/0.5.0...0.6.0

Secret release

### Fixed

-   Secret paths

## [0.5.0] - 2017-03-30

[0.5.0]: https://github.com/atomist/rug-functions-travis/compare/0.4.0...0.5.0

Rug build release

### Added

-   RugBuild function
