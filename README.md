# rug-functions-travis

Rug functions that hit the [Travis CI][travis-ci] API.  Currently
contains the following Rug functions:

-   `RestartBuild(org, buildId, token)`
-   `BuildRug(owner, repo, version, teamId, gitRef, travisToken, mavenBaseUrl, mavenUser, mavenToken, userToken)`

[travis-ci]: https://travis-ci.org/

## Building

To build, test, and install:

```
$ mvn install
```

## Releasing

1. Remove -SNAPSHOT from the pom and commit
2. Tag with version in the pom
3. Rug `mvn deploy`
4. Rev version in pom and add -SNAPSHOT on the end
5. Commit new pom
6. Push everything
