package com.atomist.rug.functions.travis

import java.io.StringReader
import java.security.{KeyFactory, Security}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

import com.atomist.rug.runtime.Rug
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.typesafe.scalalogging.LazyLogging
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.springframework.http.HttpHeaders

object Encrypt {

  Security.addProvider(new BouncyCastleProvider)

}

/**
  * Use Travis API to encrypt strings using the repository public key.
  */
class Encrypt  extends AnnotatedRugFunction
  with Rug
  with LazyLogging {

  private val travisEndpoints = new RealTravisEndpoints

  /**
    *
    * @param owner GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo name of the repo to enable
    * @param org Travis CI ".com" or ".org" endpoint
    * @param content content to encrypt
    * @param token GitHub token with "repo" scope for `owner`/`repo`
    * @return
    */
  @RugFunction(name = "travis-encrypt", description = "Encrypts a value using Travis CI repo public key",
    tags = Array(new Tag(name = "travis"), new Tag(name = "ci")))
  def encrypt(
                @Parameter(name = "owner") owner: String,
                @Parameter(name = "repo") repo: String,
                @Parameter(name = "org") org: String,
                @Parameter(name = "content") content: String,
                @Secret(name = "user_token", path = "github://user_token?scopes=repos") token: String
             ): FunctionResponse = {

    val api: TravisAPIEndpoint = TravisAPIEndpoint.stringToTravisEndpoint(org)
    val travisToken: String = travisEndpoints.postAuthGitHub(api, token)
    val headers: HttpHeaders = TravisEndpoints.authHeaders(api, travisToken)
    val repoSlug = s"$owner/$repo"
    try {
      val encryptedContent = encryptString(repoSlug, api, headers, content)
      FunctionResponse(
        Status.Success,
        Option(s"Successfully encrypted content for $repoSlug"),
        None,
        StringBodyOption(encryptedContent)
      )
    }
    catch {
      case e: Exception =>
        FunctionResponse(
          Status.Failure,
          Some(s"Failed to encrypt content for $repoSlug"),
          None,
          StringBodyOption(e.getMessage)
        )
    }
  }

  def encryptString(
                     repoSlug: String,
                     api: TravisAPIEndpoint,
                     headers: HttpHeaders,
                     content: String
                   ): String = {

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
