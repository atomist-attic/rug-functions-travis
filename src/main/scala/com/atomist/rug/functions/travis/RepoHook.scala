package com.atomist.rug.functions.travis

import java.util

import com.atomist.rug.runtime.Rug
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

/**
  * Enable and disable repos in Travis CI.
  */
class RepoHook extends AnnotatedRugFunction
  with Rug
  with LazyLogging {

  private val travisEndpoints = new RealTravisEndpoints

  /**
    * Enable Travis CI builds for a GitHub repository.
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to enable
    * @param org Travis CI ".com" or ".org" endpoint
    * @param token GitHub token with "repo" scope for `owner`/`repo`
    * @return
    */
  @RugFunction(name = "travis-enable-repo", description = "Enables Travis CI builds for a GitHub repository",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def enable(
              @Parameter(name = "owner") owner: String,
              @Parameter(name = "repo") repo: String,
              @Parameter(name = "org") org: String,
              @Secret(name = "github_token", path = "github://user_token?scopes=repos") token: String
            ): FunctionResponse = {
    val enableTravis = true
    tryRepoEnabler(enableTravis, owner, repo, token, org)
  }

  /**
    * Disable Travis CI builds for a GitHub repository.
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to disable
    * @param org Travis CI ".com" or ".org" endpoint
    * @param token GitHub token with "repo" scope for `owner`/`repo`
    * @return
    */
  @RugFunction(name = "travis-disable-repo", description = "Disables Travis CI builds for a GitHub repository",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def disable(
               @Parameter(name = "owner") owner: String,
               @Parameter(name = "repo") repo: String,
               @Parameter(name = "org") org: String,
               @Secret(name = "github_token", path = "github://user_token?scopes=repos") token: String
             ): FunctionResponse = {
    val disableTravis = false
    tryRepoEnabler(disableTravis, owner, repo, token, org)
  }

  private def tryRepoEnabler(active: Boolean, owner: String, repo: String, githubToken: String, org: String): FunctionResponse = {
    val repoSlug = s"$owner/$repo"
    val activeString = if (active) "enable" else "disable"
    try {
      repoEnabler(active, repoSlug, githubToken, org)
      FunctionResponse(Status.Success, Option(s"Successfully ${activeString}d Travis CI for $repoSlug"), None, None)
    } catch {
      case e: Exception =>
        FunctionResponse(
          Status.Failure,
          Some(s"Failed to $activeString Travis CI for $repoSlug"),
          None,
          StringBodyOption(e.getMessage)
        )
    }
  }

  private[travis] def repoEnabler(active: Boolean, repoSlug: String, githubToken: String, org: String): Unit = {
    val api: TravisAPIEndpoint = TravisAPIEndpoint.stringToTravisEndpoint(org)
    val token: String = travisEndpoints.postAuthGitHub(api, githubToken)
    val headers: HttpHeaders = TravisEndpoints.authHeaders(api, token)

    travisEndpoints.postUsersSync(api, headers)

    val id: Int = travisEndpoints.getRepo(api, headers, repoSlug)

    val hook = new util.HashMap[String, Any]()
    hook.put("id", id)
    hook.put("active", active)
    val body = new util.HashMap[String, Object]()
    body.put("hook", hook)

    travisEndpoints.putHook(api, headers, body)
  }

}
