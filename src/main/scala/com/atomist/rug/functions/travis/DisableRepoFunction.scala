package com.atomist.rug.functions.travis

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.typesafe.scalalogging.LazyLogging

/** Disable a GitHub repository in Travis CI */
class DisableRepoFunction
  extends AnnotatedRugFunction
    with TravisFunction
    with LazyLogging {

  /** Disable Travis CI builds for a GitHub repository
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to disable
    * @param githubToken GitHub token with proper scopes for Travis CI
    * @return Rug Function Response
    */
  @RugFunction(name = "travis-disable-repo", description = "Disables Travis CI builds for a GitHub repository",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def disable(
               @Parameter(name = "owner") owner: String,
               @Parameter(name = "repo") repo: String,
               @Secret(name = "githubToken", path = TravisFunction.githubTokenPath) githubToken: String
             ): FunctionResponse = {
    val disableTravis = false
    val repoSlug = RepoSlug(owner, repo)
    RepoHook(travisEndpoints, gitHubRepo).tryRepoHook(disableTravis, repoSlug, GitHubToken(githubToken))
  }

}
