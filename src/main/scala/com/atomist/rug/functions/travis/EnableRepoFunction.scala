package com.atomist.rug.functions.travis

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.typesafe.scalalogging.LazyLogging

/**
  *  Enable a GitHub repository in Travis CI
  */
class EnableRepoFunction
  extends AnnotatedRugFunction
    with TravisFunction
    with LazyLogging {

  /** Enable Travis CI builds for a GitHub repository
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to enable
    * @param githubToken GitHub token with proper scopes for Travis CI
    * @return Rug FunctionResponse
    */
  @RugFunction(name = "travis-enable-repo", description = "Enables Travis CI builds for a GitHub repository",
    tags = Array(new Tag(name = "travis-ci"), new Tag(name = "ci")))
  def enable(
              @Parameter(name = "owner") owner: String,
              @Parameter(name = "repo") repo: String,
              @Secret(name = "githubToken", path = TravisFunction.githubTokenPath) githubToken: String
            ): FunctionResponse = {
    val enableTravis = true
    val repoSlug = RepoSlug(owner, repo)
    RepoHook(travisEndpoints, gitHubRepo).tryRepoHook(enableTravis, repoSlug, GitHubToken(githubToken))
  }

}
