package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

/**
  * Build any Rug project on Travis CI using a single repo
  */
case class BuildRug(travisEndpoints: TravisEndpoints, gitHubRepo: GitHubRepo) extends LazyLogging {

  /** Build any Rug project using a single GitHub repo's Travis CI build
    *
    * @param repoSlug repository "owner/name" to be built
    * @param version version of Rug archive to publish
    * @param teamId Slack team ID connected to the GitHub owner
    * @param gitRef Git ref to checkout and build
    * @param travisToken travis-ci.com token with access to atomisthq/rug-build
    * @param mavenBaseUrl URL of Maven repository without trailing Slack team ID
    * @param mavenUser user with write access to Maven repository
    * @param mavenToken API token for Maven user
    * @param githubToken GitHub token with "repo" scope for `owner`/`repo`
    * @return Rug function response indicating success or failure
    */
  def build(repoSlug: RepoSlug,
            version: String,
            teamId: String,
            gitRef: String,
            travisToken: TravisToken,
            mavenBaseUrl: String,
            mavenUser: String,
            mavenToken: String,
            githubToken: GitHubToken): FunctionResponse = {

    val secureVars: Seq[String] = Seq(
      s"MAVEN_USER=$mavenUser",
      s"MAVEN_TOKEN=$mavenToken",
      s"GITHUB_TOKEN=$githubToken"
    )
    val api: TravisAPIEndpoint = TravisComEndpoint
    val buildRepoSlug = RepoSlug("atomisthq", "rug-build")
    val message = s"Atomist Rug build of $repoSlug"
    val headers: HttpHeaders = TravisEndpoints.authHeaders(travisToken)

    try {
      val secureEnvValues: Seq[String] = secureVars.map(encryptString(buildRepoSlug, api, headers, _))
      val secureEnvs: Seq[String] = secureEnvValues.zipWithIndex.map(ei => s"ATOMIST_SECURE${ei._2}=${ei._1}")

      val travisEnvs: Seq[String] = Seq(
        s"MAVEN_BASE_URL=$mavenBaseUrl",
        s"OWNER=${repoSlug.owner}",
        s"REPO=${repoSlug.name}",
        s"VERSION=$version",
        s"TEAM_ID=$teamId",
        s"GIT_REF=$gitRef"
      ) ++ secureEnvs

      travisEndpoints.postStartBuild(api, headers, buildRepoSlug, message, travisEnvs)
      FunctionResponse(Status.Success, Option(s"Successfully started build for $repoSlug on Travis CI"), None, None)
    } catch {
      case e: Exception =>
        logger.error(s"starting build for $repoSlug failed: ${e.getMessage}", e)
        FunctionResponse(Status.Failure, Some(s"Failed to start build for $repoSlug on Travis CI"),
          None, StringBodyOption(s"${e.getMessage}\n$e"))
    }
  }

  private def encryptString(repoSlug: RepoSlug,
                            api: TravisAPIEndpoint,
                            headers: HttpHeaders,
                            content: String): String =
    new Encrypt(travisEndpoints, gitHubRepo).encryptString(repoSlug, api, headers, content)
}
