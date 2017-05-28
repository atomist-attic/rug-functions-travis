package com.atomist.rug.functions.travis

import java.net.URI
import java.util
import java.util.Collections

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import org.springframework.http.{HttpHeaders, HttpMethod, HttpStatus, RequestEntity}
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}

/**
  *  Implement TravisEndpoints using real Travis CI endpoints
  */
class RealTravisEndpoints extends TravisEndpoints with LazyLogging {

  private val restTemplate: RestTemplate = new RestTemplate()

  import Retry._

  def getRepoKey(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): String = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.GET,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/repos/$repoSlug/key")
    )
    val responseEntity = retry("getRepoKey") {
      restTemplate.exchange(request, classOf[util.Map[String, Object]])
    }
    responseEntity.getBody.get("key").asInstanceOf[String]
  }

  def putHook(endpoint: TravisAPIEndpoint, headers: HttpHeaders, body: util.HashMap[String, Object]): Unit = {
    val url: String = s"https://api.travis-ci.${endpoint.tld}/hooks"
    // why are the URL and HTTP action specified twice?
    val request = new RequestEntity(
      body,
      headers,
      HttpMethod.PUT,
      URI.create(url)
    )
    retry("putHook") { restTemplate.put(url, request) }
  }

  def postUsersSync(endpoint: TravisAPIEndpoint, headers: HttpHeaders): Unit = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.POST,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/users/sync")
    )
    retry("postUsersSync") {
      restTemplate.exchange(request, classOf[util.Map[String, Object]])
    }
  }

  def getRepoRetryingWithSync(api: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug ): Int = {
    val id: Int = try {
      getRepoOnce(api, headers, repoSlug)
    } catch {
      case he: HttpClientErrorException if he.getStatusCode == HttpStatus.NOT_FOUND =>
        logger.warn(s"$repoSlug not found on first attempt, executing sync and retrying get")
        postUsersSync(api, headers)
        getRepo(api, headers, repoSlug)
    }
    id
  }

  private def getRepoOnce(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.GET,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/repos/$repoSlug")
    )
    val responseEntity = restTemplate.exchange(request, classOf[util.Map[String, Object]])
    val repoObject: util.Map[String, Object] = responseEntity.getBody.get("repo").asInstanceOf[util.Map[String, Object]]
    repoObject.get("id").asInstanceOf[Int]
  }

  def getRepo(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int =
    retry("getRepo") { getRepoOnce(endpoint, headers, repoSlug) }

  // Use evil var to cache token because Travis CI does not want you to
  // repeatedly get new tokens.
  private[travis] var travisTokens: util.Map[GitHubToken, TravisToken] = new util.HashMap[GitHubToken, TravisToken]()
  def postAuthGitHub(endpoint: TravisAPIEndpoint, githubToken: GitHubToken): TravisToken =
    if (travisTokens.containsKey(githubToken)) {
      travisTokens.get(githubToken)
    }
    else {
      val body: util.Map[String, Object] = Collections.singletonMap("github_token", githubToken.toString)
      val request = new RequestEntity[util.Map[String, Object]](
        body,
        TravisEndpoints.headers,
        HttpMethod.POST,
        URI.create(s"https://api.travis-ci.${endpoint.tld}/auth/github")
      )
      val responseEntity = retry("postAuthGitHub") {
        restTemplate.exchange(request, classOf[util.Map[String, String]])
      }
      val accessToken = responseEntity.getBody.get("access_token")
      val travisToken = TravisToken(accessToken)
      travisTokens.put(githubToken, travisToken)
      travisToken
    }

  def postRestartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, buildId: String): Unit = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.POST,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/builds/$buildId/restart")
    )
    retry("postRestartBuild") {
      restTemplate.exchange(request, classOf[util.Map[String, Object]])
    }
  }

  def postStartBuild(endpoint: TravisAPIEndpoint,
                     headers: HttpHeaders,
                     repoSlug: RepoSlug,
                     message: String,
                     envVars: Seq[String]): Unit = {
    val body = Collections.singletonMap[String, Object]("request", Collections.unmodifiableMap[String, Object](Map(
      "message" -> s"API initiated Travis CI build: $message",
      "branch" -> "master",
      "config" -> Collections.singletonMap[String, Object](
        "env", Collections.singletonMap[String, Object]("global", Collections.unmodifiableList[String](envVars.asJava)))
    ).asJava))
    val escapedRepoSlug = repoSlug.toString.replace("/", "%2F")
    val urlString = s"https://api.travis-ci.${endpoint.tld}/repo/$escapedRepoSlug/requests"
    headers.add("Travis-API-Version", "3")
    val request = new RequestEntity(
      body,
      headers,
      HttpMethod.POST,
      URI.create(urlString)
    )
    retry("postStartBuild") {
      restTemplate.exchange(request, classOf[util.Map[String, Object]])
    }
  }
}
