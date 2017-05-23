package com.atomist.rug.functions.travis

import java.util

import org.springframework.http.HttpHeaders

class FailTravisEndpoints extends TravisEndpoints {

  val e = new Exception("fail travis endpoint, fail")

  def getRepoKey(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): String = throw e

  def putHook(endpoint: TravisAPIEndpoint, headers: HttpHeaders, body: util.HashMap[String, Object]): Unit = throw e

  def postUsersSync(endpoint: TravisAPIEndpoint, headers: HttpHeaders): Unit = throw e

  def getRepo(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int = throw e

  def postAuthGitHub(endpoint: TravisAPIEndpoint, githubToken: GitHubToken): TravisToken = throw e

  def postRestartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, number: Int): Unit = throw e

  def postStartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug,
                     message: String, envVars: Seq[String]): Unit = throw e

  def getRepoRetryingWithSync(api: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int = throw e

}
