package com.atomist.rug.functions.travis

import com.atomist.rug.runtime.RugSupport
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

/**
  * Restart a travis-ci build
  */
class RestartBuild
  extends AnnotatedRugFunction
    with RugSupport
    with LazyLogging{

  private val travisEndpoints = new RealTravisEndpoints

  @RugFunction(name = "RestartBuild", description = "Restarts a travis build",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def invoke(@Parameter(name = "org") org: String,
             @Parameter(name = "buildId") buildId: Int,
             @Secret(name = "user_token", path = "github://user_token?scopes=repos") token: String): FunctionResponse = {

      val api: TravisAPIEndpoint = TravisAPIEndpoint.stringToTravisEndpoint(org)
      val travisToken: String = travisEndpoints.postAuthGitHub(api, token)
      val headers: HttpHeaders = TravisEndpoints.authHeaders(api, travisToken)
      try {
        travisEndpoints.postRestartBuild(api, headers, buildId)
        FunctionResponse(Status.Success, Option(s"Successfully restarted build `$buildId` on Travis CI"), None, None)
      }
      catch {
        case e: Exception =>
          FunctionResponse(Status.Failure, Some(s"Failed to restart build `$buildId` on Travis CI"), None, StringBodyOption(e.getMessage))
      }
  }
}

