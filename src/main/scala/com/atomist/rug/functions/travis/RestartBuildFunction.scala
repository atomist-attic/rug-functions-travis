package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

/**
  * Restart a travis-ci build
  */
class RestartBuildFunction
  extends AnnotatedRugFunction
    with TravisFunction
    with LazyLogging {

  /**
    * Restart a presumably failed Travis CI build.
    *
    * @param owner        GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo         name of the repo to enable
    * @param buildId      ID of build to restart
    * @param githubToken  GitHub token with proper scopes for Travis CI
    * @return
    */
  @RugFunction(name = "travis-restart-build", description = "Restarts a travis build",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def invoke(@Parameter(name = "owner") owner: String,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "buildId") buildId: String,
             @Secret(name = "githubToken", path = TravisFunction.githubTokenPath) githubToken: String): FunctionResponse = {
    val repoSlug = RepoSlug(owner, repo)
    RestartBuild(travisEndpoints, gitHubRepo).tryRestart(repoSlug, buildId, GitHubToken(githubToken))
  }
}

