package com.atomist.rug.functions.travis

import java.net.URI
import java.util

import org.springframework.http.{HttpHeaders, HttpMethod, HttpStatus, RequestEntity}
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}

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
      try {
        restTemplate.exchange(request, classOf[util.Map[String, Object]])
      } catch {
        case e: HttpClientErrorException if e.getStatusCode == HttpStatus.UNAUTHORIZED =>
          throw new DoNotRetryException(s"Not Authorized to get repo ${repoSlug} from github", e)
      }
    }
    responseEntity.getBody
  }

}
