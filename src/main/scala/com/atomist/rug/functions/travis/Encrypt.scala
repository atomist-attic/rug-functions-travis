package com.atomist.rug.functions.travis

import java.io.StringReader
import java.security.{KeyFactory, Security}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

import com.atomist.rug.spi.{FunctionResponse, StringBodyOption}
import com.atomist.rug.spi.Handlers.Status
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.springframework.http.HttpHeaders

/**
  * Encrypt values for Travis CI
  */
case class Encrypt(travisEndpoints: TravisEndpoints) {

  /** Fetch Travis public key for repo and encrypt content
    *
    * @param owner   GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo    name of the repo to enable
    * @param org     Travis CI ".com" or ".org" endpoint
    * @param content content to encrypt
    * @param token   GitHub token with "repo" scope for `owner`/`repo`
    * @return FunctionResponse with encrypted content in the body
    */
  def tryEncrypt(owner: String,
                 repo: String,
                 org: String,
                 content: String,
                 token: String): FunctionResponse = {
    val repoSlug = s"$owner/$repo"
    try {
      val api: TravisAPIEndpoint = TravisAPIEndpoint.stringToTravisEndpoint(org)
      val headers = if (TravisAPIEndpoint.isPublic(org)) {
        TravisEndpoints.headers
      } else {
        val travisToken: String = travisEndpoints.postAuthGitHub(api, token)
        TravisEndpoints.authHeaders(travisToken)
      }
      val encryptedContent = encryptString(repoSlug, api, headers, content)
      FunctionResponse(
        Status.Success,
        Option(s"Successfully encrypted content for $repoSlug"),
        None,
        StringBodyOption(encryptedContent)
      )
    } catch {
      case e: Exception =>
        FunctionResponse(
          Status.Failure,
          Some(s"Failed to encrypt content for $repoSlug"),
          None,
          StringBodyOption(e.getMessage)
        )
    }
  }

  def encryptString(repoSlug: String,
                    api: TravisAPIEndpoint,
                    headers: HttpHeaders,
                    content: String): String = {

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

object Encrypt {

  Security.addProvider(new BouncyCastleProvider)

}
