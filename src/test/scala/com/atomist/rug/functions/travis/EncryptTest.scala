package com.atomist.rug.functions.travis

import java.io.StringReader
import java.security.{KeyFactory, Security}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64
import javax.crypto.Cipher

import com.atomist.rug.spi.Handlers.Status
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.{PEMKeyPair, PEMParser}
import org.scalatest.{FlatSpec, Matchers}

class EncryptTest extends FlatSpec with Matchers {

  val msg = "Bottle Up and Explode!"

  private def decryptString(encryptedString: String): String = {
    val parser = new PEMParser(new StringReader(MockTravisEndpoints.mockPrivateKey))
    val ob = parser.readObject().asInstanceOf[PEMKeyPair]

    val privateKeySpec = new PKCS8EncodedKeySpec(ob.getPrivateKeyInfo().getEncoded())
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKey = keyFactory.generatePrivate(privateKeySpec)

    val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)

    val decoded = Base64.getDecoder.decode(encryptedString)
    val decrypted = rsaCipher.doFinal(decoded)
    new String(decrypted, "UTF-8")
  }

  val repoSlug = RepoSlug("noone", "nothing")

  it should "encrypt a simple message" in {
    val encrypt = new Encrypt(new MockTravisEndpoints, new MockGitHubRepo)
    val headers = TravisEndpoints.authHeaders(TravisToken("notaghtoken"))
    val result = encrypt.encryptString(repoSlug, TravisOrgEndpoint, headers, msg)
    assert(decryptString(result) === msg)
  }

  it should "respond with encrypted message in response body" in {
    val encrypt = new Encrypt(new MockTravisEndpoints, new MockGitHubRepo)
    val resp = encrypt.tryEncrypt(repoSlug, msg, GitHubToken("bunktoken"))
    assert(resp.status === Status.Success)
    resp.body match {
      case Some(b) => b.str match {
        case Some(s) if decryptString(s) === msg =>
        case Some(x) => fail(s"encrypted message did not match expected: $x")
        case None => fail(s"response body did not contain encrypted message")
      }
      case None => fail(s"response did not have a body")
    }
  }

  it should "properly fail if travis api fails" in {
    val encrypt = new Encrypt(new FailTravisEndpoints, new MockGitHubRepo)
    val resp = encrypt.tryEncrypt(repoSlug, msg, GitHubToken("invalidtoken"))
    assert(resp.status === Status.Failure)
  }

}

object EncryptTest {

  Security.addProvider(new BouncyCastleProvider());

}
