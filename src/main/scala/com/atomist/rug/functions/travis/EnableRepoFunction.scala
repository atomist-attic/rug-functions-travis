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
    * @param org Travis CI ".com" or ".org" endpoint
    * @param token GitHub token with "repo" scope for `owner`/`repo`
    * @return Rug FunctionResponse
    */
  @RugFunction(name = "travis-enable-repo", description = "Enables Travis CI builds for a GitHub repository",
    tags = Array(new Tag(name = "travis-ci"), new Tag(name = "ci")))
  def enable(
              @Parameter(name = "owner") owner: String,
              @Parameter(name = "repo") repo: String,
              @Parameter(name = "org") org: String,
              @Secret(name = "github_token", path = "github://user_token?scopes=repo") token: String
            ): FunctionResponse = {
    val enableTravis = true
    RepoHook(travisEndpoints).tryRepoHook(enableTravis, owner, repo, token, org)
  }

}
