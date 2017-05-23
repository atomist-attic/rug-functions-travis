package com.atomist.rug.functions.travis

import java.net.URI
import java.util

import org.springframework.http.{HttpHeaders, HttpMethod, RequestEntity}
import org.springframework.web.client.RestTemplate

/**
  * Really hit the GitHub v3 API.
  */
class RealGitHubRepo extends GitHubRepo {

  private val restTemplate: RestTemplate = new RestTemplate()

  override def getRepo(repoSlug: RepoSlug, token: GitHubToken): util.Map[String, Object] = {
    val headers = new HttpHeaders()
    headers.add("Authorization", s"token $token")
    headers.add("Accept", "application/vnd.github.v3+json")

    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.GET,
      URI.create(s"https://api.github.com/repos/$repoSlug")
    )
    val responseEntity = Retry.retry("GitHub.getRepo") {
      restTemplate.exchange(request, classOf[util.Map[String, Object]])
    }
    responseEntity.getBody
  }

}
