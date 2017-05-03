package com.atomist.rug.functions.travis

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}

/**
  * Build any Rug repository from a single builder repository
  */
class BuildRugFunction
  extends AnnotatedRugFunction
    with TravisFunction {

  /**
    * Execute travis-build-rug Rug function, which builds an arbitrary Rug archive
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to be built
    * @param version version of Rug archive to publish
    * @param teamId Slack team ID connected to the GitHub owner
    * @param gitRef Git ref to checkout and build
    * @param travisToken travis-ci.com token with access to atomisthq/rug-build
    * @param mavenBaseUrl URL of Maven repository without trailing Slack team ID
    * @param mavenUser user with write access to Maven repository
    * @param mavenToken API token for Maven user
    * @param githubToken GitHub token with proper scopes for Travis CI
    * @return Rug function reponse indicating success or failure
    */
  @RugFunction(name = "travis-build-rug", description = "builds a Rug archive on Travis CI using rug-build",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def build(@Parameter(name = "owner") owner: String,
            @Parameter(name = "repo") repo: String,
            @Parameter(name = "version") version: String,
            @Parameter(name = "teamId") teamId: String,
            @Parameter(name = "gitRef") gitRef: String,
            @Secret(name = "travisToken", path = "secret://team?path=travis_token") travisToken: String,
            @Secret(name = "mavenBaseUrl", path = "secret://team?path=maven_base_url") mavenBaseUrl: String,
            @Secret(name = "mavenUser", path = "secret://team?path=maven_user") mavenUser: String,
            @Secret(name = "mavenToken", path = "secret://team?path=maven_token") mavenToken: String,
            @Secret(name = "githubToken", path = TravisFunction.githubTokenPath) githubToken: String): FunctionResponse =
    BuildRug(travisEndpoints).build(owner, repo, version, teamId, gitRef, travisToken, mavenBaseUrl,
      mavenUser, mavenToken, githubToken)
}
