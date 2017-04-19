package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{FunctionResponse, StringBodyOption}
import org.springframework.http.HttpHeaders

/**
  * Build any Rug project on Travis CI using a single repo
  */
case class BuildRug(travisEndpoints: TravisEndpoints) {

  /** Build any Rug project using a single GitHub repo's Travis CI build
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to be built
    * @param version version of Rug archive to publish
    * @param teamId Slack team ID connected to the GitHub owner
    * @param gitRef Git ref to checkout and build
    * @param travisToken travis-ci.com token with access to atomisthq/rug-build
    * @param mavenBaseUrl URL of Maven repository without trailing Slack team ID
    * @param mavenUser user with write access to Maven repository
    * @param mavenToken API token for Maven user
    * @param userToken GitHub token with "repo" scope for `owner`/`repo`
    * @return Rug function response indicating success or failure
    */
  def build(owner: String,
            repo: String,
            version: String,
            teamId: String,
            gitRef: String,
            travisToken: String,
            mavenBaseUrl: String,
            mavenUser: String,
            mavenToken: String,
            userToken: String): FunctionResponse = {

    val secureVars: Seq[String] = Seq(
      s"MAVEN_USER=$mavenUser",
      s"MAVEN_TOKEN=$mavenToken",
      s"GITHUB_TOKEN=$userToken"
    )
    val api: TravisAPIEndpoint = TravisComEndpoint
    val buildRepoSlug = "atomisthq/rug-build"
    val message = s"Atomist Rug build of $owner/$repo"
    val headers: HttpHeaders = TravisEndpoints.authHeaders(travisToken)

    try {
      val secureEnvValues: Seq[String] = secureVars.map(encryptString(buildRepoSlug, api, headers, _))
      val secureEnvs: Seq[String] = secureEnvValues.zipWithIndex.map(ei => s"ATOMIST_SECURE${ei._2}=${ei._1}")

      val travisEnvs: Seq[String] = Seq(
        s"MAVEN_BASE_URL=$mavenBaseUrl",
        s"OWNER=$owner",
        s"REPO=$repo",
        s"VERSION=$version",
        s"TEAM_ID=$teamId",
        s"GIT_REF=$gitRef"
      ) ++ secureEnvs

      travisEndpoints.postStartBuild(api, headers, buildRepoSlug, message, travisEnvs)
      FunctionResponse(Status.Success, Option(s"Successfully started build for $owner/$repo on Travis CI"), None, None)
    } catch {
      case e: Exception =>
        FunctionResponse(Status.Failure, Some(s"Failed to start build for $owner/$repo on Travis CI"),
          None, StringBodyOption(e.getMessage))
    }
  }

  private def encryptString(repoSlug: String,
                            api: TravisAPIEndpoint,
                            headers: HttpHeaders,
                            content: String): String =
    new Encrypt(travisEndpoints).encryptString(repoSlug, api, headers, content)
}
