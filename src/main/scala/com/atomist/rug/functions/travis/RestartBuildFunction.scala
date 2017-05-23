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
    * @param visibility   whether repo/build is "public" or "private", used to determine which Travis API to hit
    * @param buildId      ID of build to restart
    * @param githubToken  GitHub token with proper scopes for Travis CI
    * @return
    */
  @RugFunction(name = "restart-travis-build", description = "Restarts a travis build",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def invoke(@Parameter(name = "visibility") visibility: String,
             @Parameter(name = "buildId") buildId: Int,
             @Secret(name = "githubToken", path = TravisFunction.githubTokenPath) githubToken: String): FunctionResponse = {

      val api: TravisAPIEndpoint = TravisAPIEndpoint.stringToTravisEndpoint(visibility)
      val travisToken = travisEndpoints.postAuthGitHub(api, GitHubToken(githubToken))
      val headers: HttpHeaders = TravisEndpoints.authHeaders(travisToken)
      try {
        travisEndpoints.postRestartBuild(api, headers, buildId)
        FunctionResponse(Status.Success, Option(s"Successfully restarted build `$buildId` on Travis CI"), None, None)
      }
      catch {
        case e: Exception =>
          logger.error(s"$visibility build $buildId restart failed: ${e.getMessage}", e)
          FunctionResponse(Status.Failure, Some(s"Failed to restart build `$buildId` on Travis CI"), None, StringBodyOption(e.getMessage))
      }
  }
}

