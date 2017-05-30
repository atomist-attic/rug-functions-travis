package com.atomist.rug.functions.travis

import com.atomist.rug.spi.{FunctionResponse, StringBodyOption}
import com.atomist.rug.spi.Handlers.Status
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

/**
  * Restart failed Travis CI builds.
  */
case class RestartBuild (travisEndpoints: TravisEndpoints, gitHubRepo: GitHubRepo)
  extends LazyLogging {

  def tryRestart(repoSlug: RepoSlug, buildId: String, githubToken: GitHubToken): FunctionResponse = {
    try {
      postBuildRestart(repoSlug, buildId, githubToken)
      FunctionResponse(Status.Success, Option(s"Successfully restarted $repoSlug build `$buildId` on Travis CI"),
        None, None)
    } catch {
      case e: Exception =>
        logger.error(s"$repoSlug build $buildId restart failed: ${e.getMessage}", e)
        FunctionResponse(Status.Failure, Some(s"Failed to restart $repoSlug build `$buildId` on Travis CI"),
          None, StringBodyOption(e.getMessage))
    }
  }

  private def postBuildRestart(repoSlug: RepoSlug, buildId: String, githubToken: GitHubToken): Unit = {
    val api = gitHubRepo.travisEndpoint(repoSlug, githubToken)
    val travisToken = travisEndpoints.postAuthGitHub(api, githubToken)
    val headers: HttpHeaders = TravisEndpoints.authHeaders(travisToken)
    travisEndpoints.postRestartBuild(api, headers, buildId)
  }

}
