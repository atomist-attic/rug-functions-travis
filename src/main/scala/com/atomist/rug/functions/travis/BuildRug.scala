package com.atomist.rug.functions.travis

import java.io.StringReader
import java.security.{KeyFactory, Security}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import com.atomist.rug.runtime.RugSupport
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.http.HttpHeaders

object BuildRug {

  Security.addProvider(new BouncyCastleProvider)
}

class BuildRug extends AnnotatedRugFunction
  with RugSupport
  with LazyLogging{

  private val travisEndpoints = new RealTravisEndpoints

  /**
    * Execute travis-build-rug Rug function, which builds an arbitrary Rug archive
    * @param owner GitHub owner of the repo to be built
    * @param repo name of the repo to be built
    * @param version version of Rug archive to publish
    * @param teamId Slack team ID connected to the GitHub owner
    * @param gitRef Git ref to checkout and build
    * @param travisToken travis-ci.com token with access to atomisthq/rug-build
    * @param mavenBaseUrl URL of Maven repository without trailing Slack team ID
    * @param mavenUser user with write access to Maven repository
    * @param mavenToken API token for Maven user
    * @param userToken GitHub token with "repo" scope for `owner`/`repo`
    * @return
    */
  @RugFunction(name = "travis-build-rug", description = "builds a Rug archive on Travis CI using rug-build",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def invoke(
              @Parameter(name = "owner") owner: String,
              @Parameter(name = "repo") repo: String,
              @Parameter(name = "version") version: String,
              @Parameter(name = "teamId") teamId: String,
              @Parameter(name = "gitRef") gitRef: String,
              @Secret(name = "travisToken", path = "secret://team?path=travis_token") travisToken: String,
              @Secret(name = "mavenBaseUrl", path = "secret://team?path=maven_base_url") mavenBaseUrl: String,
              @Secret(name = "mavenUser", path = "secret://team?path=maven_user") mavenUser: String,
              @Secret(name = "mavenToken", path = "secret://team?path=maven_token") mavenToken: String,
              @Secret(name = "userToken", path = "github://user_token?scopes=repo") userToken: String): FunctionResponse = {

    val secureVars: Seq[String] = Seq(
      s"MAVEN_USER=$mavenUser",
      s"MAVEN_TOKEN=$mavenToken",
      s"GITHUB_TOKEN=$userToken"
    )
    val api: TravisAPIEndpoint = TravisComEndpoint
    val buildRepoSlug = "atomisthq/rug-build"
    val message = s"Atomist Rug build of $owner/$repo"
    val headers: HttpHeaders = TravisEndpoints.authHeaders(api, travisToken)
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

    try {
      travisEndpoints.postStartBuild(api, headers, buildRepoSlug, message, travisEnvs)
      FunctionResponse(Status.Success, Option(s"Successfully started build for $owner/$repo on Travis CI"), None, None)
    }
    catch {
      case e: Exception =>
        FunctionResponse(Status.Failure, Some(s"Failed to start build for $owner/$repo on Travis CI"), None, StringBodyOption(e.getMessage))
    }
  }

  private[travis] def encryptString(repoSlug: String, api: TravisAPIEndpoint, headers: HttpHeaders, content: String): String = {
    val key = travisEndpoints.getRepoKey(api, headers, repoSlug)

    val parser = new PEMParser(new StringReader(key))
    val ob = parser.readObject().asInstanceOf[SubjectPublicKeyInfo]

    val pubKeySpec = new X509EncodedKeySpec(ob.getEncoded())
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKey = keyFactory.generatePublic(pubKeySpec)

    val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)

    Base64.getEncoder.encodeToString(rsaCipher.doFinal(content.getBytes()))
  }
}
