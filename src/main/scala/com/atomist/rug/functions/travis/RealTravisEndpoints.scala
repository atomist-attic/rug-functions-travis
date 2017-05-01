package com.atomist.rug.functions.travis

import java.net.URI
import java.util
import java.util.Collections

import scala.collection.JavaConverters._
import org.springframework.http.{HttpHeaders, HttpMethod, HttpStatus, RequestEntity}
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}

import scala.util.control.Breaks.{break, breakable}

/**
  *  Implement TravisEndpoints using real Travis CI endpoints
  */
class RealTravisEndpoints extends TravisEndpoints {

  private val restTemplate: RestTemplate = new RestTemplate()

  def getRepoKey(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String): String = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.GET,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/repos/$repoSlug/key")
    )
    val responseEntity = restTemplate.exchange(request, classOf[util.Map[String, Object]])
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
    restTemplate.put(url, request)
  }

  def postUsersSync(endpoint: TravisAPIEndpoint, headers: HttpHeaders): Unit = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.POST,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/users/sync")
    )
    try {
      restTemplate.exchange(request, classOf[util.Map[String, Object]])
    }
    catch {
      case he: HttpClientErrorException if he.getStatusCode == HttpStatus.CONFLICT =>
    }
    import scala.util.control.Breaks._
    breakable {
      for (i <- 0 to 30) {
        try {
          val responseEntity = restTemplate.exchange(request, classOf[util.Map[String, Object]])
          if (responseEntity.getStatusCode == HttpStatus.OK) {
            break
          }
        }
        catch {
          case he: HttpClientErrorException if he.getStatusCode == HttpStatus.CONFLICT =>
            print(s"  Waiting for repositories to sync ($i)")
            Thread.sleep(1000L)
          case _: Throwable => break
        }
      }
    }
  }

  def getRepoRetryingWithSync(api: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String ): Int = {

    val id: Int = try {
      getRepo(api, headers, repoSlug)
    } catch {
      case he: HttpClientErrorException if he.getStatusCode == HttpStatus.NOT_FOUND =>
        postUsersSync(api, headers)
        var repoId = 0
        breakable {
          for (i <- 0 to 30) {
            try {
              repoId = getRepo(api, headers, repoSlug)
              break
            }
            catch {
              case he: HttpClientErrorException if he.getStatusCode == HttpStatus.NOT_FOUND =>
                print(s"  Waiting for repository to become available ($i)")
                Thread.sleep(1000L)
              case _: Throwable => break
            }
          }
        }
        repoId
    }
    id
  }

  def getRepo(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String): Int = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.GET,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/repos/$repoSlug")
    )
    val responseEntity = restTemplate.exchange(request, classOf[util.Map[String, Object]])
    val repoObject: util.Map[String, Object] = responseEntity.getBody.get("repo").asInstanceOf[util.Map[String, Object]]
    repoObject.get("id").asInstanceOf[Int]
  }

  // Use evil var to cache token because Travis CI does not want you to
  // repeatedly get new tokens.
  private[travis] var travisTokens: util.Map[String, String] = new util.HashMap[String, String]()
  def postAuthGitHub(endpoint: TravisAPIEndpoint, githubToken: String): String =
    if (travisTokens.containsKey(githubToken)) {
      travisTokens.get(githubToken)
    }
    else {
      val request = new RequestEntity(
        Collections.singletonMap("github_token", githubToken),
        TravisEndpoints.headers,
        HttpMethod.POST,
        URI.create(s"https://api.travis-ci.${endpoint.tld}/auth/github")
      )
      val responseEntity = restTemplate.exchange(request, classOf[util.Map[String, String]])
      val travisToken = responseEntity.getBody.get("access_token")
      travisTokens.put(githubToken, travisToken)
      travisToken
    }

  def postRestartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, number: Int): Unit = {
    val request = new RequestEntity[util.Map[String, Object]](
      headers,
      HttpMethod.POST,
      URI.create(s"https://api.travis-ci.${endpoint.tld}/builds/$number/restart")
    )
    restTemplate.exchange(request, classOf[util.Map[String, Object]])
  }

  def postStartBuild(
                      endpoint: TravisAPIEndpoint,
                      headers: HttpHeaders,
                      repoSlug: String,
                      message: String,
                      envVars: Seq[String]): Unit = {
    val body = Collections.singletonMap[String, Object]("request", Collections.unmodifiableMap[String, Object](Map(
      "message" -> s"API initiated Travis CI build: $message",
      "branch" -> "master",
      "config" -> Collections.singletonMap[String, Object](
        "env", Collections.singletonMap[String, Object]("global", Collections.unmodifiableList[String](envVars.asJava)))
    ).asJava))
    val escapedRepoSlug = repoSlug.replace("/", "%2F")
    val urlString = s"https://api.travis-ci.${endpoint.tld}/repo/$escapedRepoSlug/requests"
    headers.add("Travis-API-Version", "3")
    val request = new RequestEntity(
      body,
      headers,
      HttpMethod.POST,
      URI.create(urlString)
    )
    restTemplate.exchange(request, classOf[util.Map[String, Object]])
  }
}
