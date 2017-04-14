package com.atomist.rug.functions.travis

import java.net.URI
import java.util
import java.util.Collections
import scala.collection.JavaConverters._

import com.atomist.rug.InvalidRugParameterPatternException
import org.springframework.http.{HttpHeaders, HttpMethod, HttpStatus, RequestEntity}
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}

trait TravisAPIEndpoint {

  def tld: String

}

object TravisAPIEndpoint {

  def stringToTravisEndpoint(ep: String): TravisAPIEndpoint = ep match {
    case ".org" | "org" => TravisOrgEndpoint
    case ".com" | "com" => TravisComEndpoint
    case _ => throw new InvalidRugParameterPatternException("Travis CI endpoint must be 'org' or 'com'")
  }

  def isPublic(ep: String): Boolean = ep match {
    case ".org" | "org" => true
    case ".com" | "com" => false
    case _ => throw new InvalidRugParameterPatternException("Travis CI endpoint must be 'org' or 'com'")
  }

}

object TravisOrgEndpoint extends TravisAPIEndpoint {
  val tld: String = "org"
}

object TravisComEndpoint extends TravisAPIEndpoint {
  val tld: String = "com"
}

trait TravisEndpoints {
  /** Return the key of the repo.
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param repoSlug repo slug, e.g., "owner/name"
    * @return Repo key
    */
  def getRepoKey(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String): String

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
  def getRepo(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String): Int

  /** Authenticate with Travis using GitHub token.
    *
    * Careful, these do not seem to expire.
    *
    * @param endpoint org|com
    * @param githubToken GitHub personal access token with appropriate scope for endpoint
    * @return Travis token
    */
  def postAuthGitHub(endpoint: TravisAPIEndpoint, githubToken: String): String

  /** Restart the build on Travis CI
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param number number of build to restart
    */
  def postRestartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, number: Int): Unit

  /** Start a new build on Travis CI
    *
    * @param endpoint org|com
    * @param headers standard Travis API headers
    * @param repoSlug repo slug, e.g., "atomist-rugs/rug-editors"
    */
  def postStartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String, message: String, envVars: Seq[String]): Unit
}

object TravisEndpoints {

  /** Construct standard Travis API headers.
    *
    * @param endpoint org|com
    * @return Travis API headers
    */
  def headers(endpoint: TravisAPIEndpoint): HttpHeaders = {
    val noAuthHeaders = new HttpHeaders()
    noAuthHeaders.add("Content-Type", "application/json")
    noAuthHeaders.add("Accept", "application/vnd.travis-ci.2+json")
    noAuthHeaders.add("User-Agent", "Travis/1.6.8")
    noAuthHeaders
  }

  /** Construct standard Travis API headers with Authorization token.
    *
    * @param endpoint org|com
    * @param token Travis API token returned from TravisEndpoints.postAuthGitHub
    * @return Travis API headers
    */
  def authHeaders(endpoint: TravisAPIEndpoint, token: String): HttpHeaders = {
    val hdrs = headers(endpoint)
    hdrs.add("Authorization", s"""token "$token"""")
    hdrs
  }
}

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
        getRepo(api, headers, repoSlug)
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
        TravisEndpoints.headers(endpoint),
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

  def postStartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: String, message: String, envVars: Seq[String]): Unit = {
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
