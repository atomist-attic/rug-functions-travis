package com.atomist.rug.functions.travis

import java.util

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

/**
  * Enable and disable repositories in Travis CI
  */
case class RepoHook(travisEndpoints: TravisEndpoints, gitHubRepo: GitHubRepo)
  extends LazyLogging {

  def tryRepoHook(active: Boolean, repoSlug: RepoSlug, githubToken: GitHubToken): FunctionResponse = {
    val activeString = if (active) "enable" else "disable"
    try {
      authPutHook(active, repoSlug, githubToken)
      FunctionResponse(Status.Success, Option(s"Successfully ${activeString}d Travis CI for $repoSlug"), None, None)
    } catch {
      case e: Exception =>
        logger.error(s"$activeString for $repoSlug failed: ${e.getMessage}", e)
        FunctionResponse(
          Status.Failure,
          Some(s"Failed to $activeString Travis CI for $repoSlug"),
          None,
          StringBodyOption(e.getMessage)
        )
    }
  }

  private def authPutHook(active: Boolean, repoSlug: RepoSlug, githubToken: GitHubToken): Unit = {
    val api = gitHubRepo.travisEndpoint(repoSlug, githubToken)
    val token = travisEndpoints.postAuthGitHub(api, githubToken)
    val headers = TravisEndpoints.authHeaders(token)

    val id: Int = travisEndpoints.getRepoRetryingWithSync(api, headers, repoSlug)

    val hook = new util.HashMap[String, Any]()
    hook.put("id", id)
    hook.put("active", active)
    val body = new util.HashMap[String, Object]()
    body.put("hook", hook)

    travisEndpoints.putHook(api, headers, body)
  }

}
