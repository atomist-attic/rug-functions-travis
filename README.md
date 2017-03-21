# rug-functions-travis

Currently contains a single Rug Function: RestartBuild (org, buildId, token)

# Building

Currently there is no travis build. 

To build:

```shell
mvn install
```

# Releasing

1. Remove -SNAPSHOT from the pom and commit
2. Tag with version in the pom
3. Rug `mvn deploy`
4. Rev version in pom and add -SNAPSHOT on the end
5. Commit new pom
6. Push everything
