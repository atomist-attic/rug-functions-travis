package com.atomist.rug.functions.travis

import java.util

import org.springframework.http.HttpHeaders

trait TravisEndpoints {

  /** Return the public SSH key of the repo.
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param repoSlug repo slug, e.g., "owner/name"
    * @return Repo key
    */
  def getRepoKey(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): String

  /** Enable or disable CI for the repo.
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param body hook body see Travis API docs
    */
  def putHook(endpoint: TravisAPIEndpoint, headers: HttpHeaders, body: util.HashMap[String, Object]): Unit

  /** Refresh the list of repos available to user in Travis CI.
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    */
  def postUsersSync(endpoint: TravisAPIEndpoint, headers: HttpHeaders): Unit

  /** Get repo unique integer identifier.
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param repoSlug repo slug, e.g., "owner/name"
    * @return unique repo identifier
    */
  def getRepo(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int

  /** Authenticate with Travis using GitHub token.
    *
    * Careful, these do not seem to expire.
    *
    * @param endpoint org|com
    * @param githubToken GitHub personal access token with appropriate scope for endpoint
    * @return Travis token
    */
  def postAuthGitHub(endpoint: TravisAPIEndpoint, githubToken: GitHubToken): TravisToken

  /** Restart the build on Travis CI
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param buildId identifier of build to restart
    */
  def postRestartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, buildId: String): Unit

  /** Start a new build on Travis CI
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param repoSlug repo slug, e.g., "atomist-rugs/rug-editors"
    * @param message build message, i.e., message displayed at the top of the
    *                build page on Travis CI
    * @param envVars global environment variables to supply to build that will override
    *                any global environment variables in the build's .travis.yml
    */
  def postStartBuild(
                      endpoint: TravisAPIEndpoint,
                      headers: HttpHeaders,
                      repoSlug: RepoSlug,
                      message: String,
                      envVars: Seq[String]): Unit

  /** Get the repository identifier from Travis CI.  If the initial attempt
    * fails, sync repositories and try again.
    *
    * @param api org|com
    * @param headers standard Travis API headers
    * @param repoSlug repo slug, e.g., "atomist-rugs/rug-editors"
    * @return Travis CI repository identifier
    */
  def getRepoRetryingWithSync(api: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int

}

object TravisEndpoints {

  /** Construct standard Travis API headers.
    *
    * @return Travis API headers
    */
  def headers: HttpHeaders = {
    val noAuthHeaders = new HttpHeaders()
    noAuthHeaders.add("Content-Type", "application/json")
    noAuthHeaders.add("Accept", "application/vnd.travis-ci.2+json")
    noAuthHeaders.add("User-Agent", "Travis/1.6.8")
    noAuthHeaders
  }

  /** Construct standard Travis API headers with Authorization token.
    *
    * @param token Travis API token returned from TravisEndpoints.postAuthGitHub
    * @return Travis API headers
    */
  def authHeaders(token: TravisToken): HttpHeaders = {
    val hdrs = headers
    hdrs.add("Authorization", s"""token "$token"""")
    hdrs
  }

}
