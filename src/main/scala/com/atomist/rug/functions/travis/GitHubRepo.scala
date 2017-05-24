package com.atomist.rug.functions.travis

import java.util

/**
  * GitHub repository trait.
  */
trait GitHubRepo {

  /** Return https://developer.github.com/v3/repos/#get
    *
    * @param repoSlug repostory "owner/name"
    * @param token GitHub personal access token with access to repoSlug
    * @return parsed response from GitHub GET repo endpoint
    */
  def getRepo(repoSlug: RepoSlug, token: GitHubToken): util.Map[String, Object]

  /** Determine correct Travis API endpoint for a GitHub.com repository.
    *
    * @param repoSlug repostory "owner/name"
    * @param token GitHub personal access token with access to repoSlug
    * @return TravisAPIEndpoint to use with repoSlug
    */
  def travisEndpoint(repoSlug: RepoSlug, token: GitHubToken): TravisAPIEndpoint = {
    val repo = getRepo(repoSlug, token)
    val isPrivate = repo.get("private").asInstanceOf[Boolean]
    if (isPrivate) TravisComEndpoint
    else TravisOrgEndpoint
  }

}
