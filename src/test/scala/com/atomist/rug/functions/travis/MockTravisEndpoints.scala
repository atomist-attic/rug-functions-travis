package com.atomist.rug.functions.travis

import java.util

import org.springframework.http.HttpHeaders

object MockTravisEndpoints {
  // we cannot use a static value for the encrypted message
  // because https://en.wikipedia.org/wiki/RSA_(cryptosystem)#Padding_schemes
  // do not use this private key for anything real
  val mockPrivateKey = """-----BEGIN RSA PRIVATE KEY-----
                         |MIIEowIBAAKCAQEAtp/Bix4/Px0skmWkaCLSYhwzuw4yrOCVdZf2dYC7k0CR2FnQ
                         |UpRCGsfo1JAX2eo+V/WXqXkbrJpesh+1MqxsLCKqngMmciR6zx/gybQXSfaq3HgC
                         |yrLDcY9nQqICsHnnKW7FVaL7riCb2cyA52ebu1tyBpSEiXx8EFF65zY7Z+q1jcfO
                         |D0k7a0EpnlS+EJHYpsE8L83XFNq8vXzTAHkjvQhaze5jk42r7nVxfc3GIBvq0Jnh
                         |eNw9dTAmNBfgBVKBSNaPvo3NymsK86MrVkyWcf4YbO6qe6/Yb7z2VJYFzgUGypri
                         |OmgNJsmPz/P/+D0fyAZJGbI9mf9CwwCdUce3/QIDAQABAoIBAGb98cjjZgfSDDED
                         |4ZRZDw0FmqcDetDEV4XaDcR2tVJ2N8or3LC1rBIua8B1Av6CfGZeuwl4o4LUDFOo
                         |Tigl1VuOsWornKMCr7/f2oXmarvrwLBVfj2SU2bX2QRbGeks1pEnR2LkZ8vr1/kc
                         |vBXE+K7RA51yZMykx9LsIMQEcbxITMoUAHOV3U6hGzfdNaJonql4tbq8zVaZyTY0
                         |xH3mRkdGUXusbihr6OonVq+twB+GJ5knJ2+aNoBAJ08C0djui4v1dNXf6hPOtP48
                         |WhP1mSu04nnrmiySQWJNyAmL+JUVBlzUB0TsDTsshTfQtUpxZguOZoJ/ziIGUAg8
                         |MGbCGoECgYEA7p4kDMH/B2dnsvhDq/FuW6hxunRuqTrSje1tdICtfHMG9ejiTKiI
                         |eoHliYvOZZFStaXQAcoKNaXnLTleYlagHpaphAyvif610sf9bGrQ+pQH9s393d67
                         |ta3GoUJkMNoZcvDdVt8bPq6I30z4Hvh76deiRMF3oS8YFKux4sDh8ecCgYEAw+1q
                         |8OcttzKgp3WUMO7VBLyKsQd9RFM48jFgTmlVtZonWHgDJ9cdArnNvTrjVyge71qP
                         |zHa7miI+uXvfm6JIL8F2Z5lX8sI6JT2zwz1iB1sX8d1Y1U+8Lz/AZqQewXDVcdCx
                         |GpaRK0n437En5T5vFnyM5Ws/Jn1hLm7+i1XP0nsCgYARv+u4kzmwSE3bb0JBaQ0n
                         |fkkvcHfG2NxOuGma7/N3vWq4IiGrSCIW0tDLQX4R6hR39KSbbXcC9JtUrt7Je94f
                         |SF/Ftdfc8Ph/fGbqiKuQ6DALeNk4htf5tLqAxlqDk8Wu2iHs013IdN0zlxsh2qQF
                         |CghFCwsmD0XAS+FIl8Z24wKBgEmGhUVWXA+NzkBJnY0nc4VNg/afSuEjIhGxeeSz
                         |HtkBupY2o2iGD3sAYzcKLFp+0e0c3S3ruMdE5qkQ1X9ATTqurVJ/d0PAo7VqDFXO
                         |aUU9aCT53eZe/83zbK6YFHqfb1pA6NWDf4LxRZYck04yOdoEb5OAxbgaASg9uwRq
                         |9YyVAoGBAICFOPYVh2ZeXd79xsH7GCwFkVxmk6Y4U5taBZqBaZ3P90nG9OMLbDL0
                         |6AGynrzGjwpRMN1Ez8CL8froJtv0ydX6lbuRTDau79iaF7FZmSNQtwhJcul3v/h7
                         |IPrAO+i7WyqB5L4j2hEJ3qkIhtRmVgF8iCxxhLHcJXmRrqwvQnN/
                         |-----END RSA PRIVATE KEY-----
                         |""".stripMargin
  val mockRepoKey = """-----BEGIN PUBLIC KEY-----
                      |MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtp/Bix4/Px0skmWkaCLS
                      |Yhwzuw4yrOCVdZf2dYC7k0CR2FnQUpRCGsfo1JAX2eo+V/WXqXkbrJpesh+1Mqxs
                      |LCKqngMmciR6zx/gybQXSfaq3HgCyrLDcY9nQqICsHnnKW7FVaL7riCb2cyA52eb
                      |u1tyBpSEiXx8EFF65zY7Z+q1jcfOD0k7a0EpnlS+EJHYpsE8L83XFNq8vXzTAHkj
                      |vQhaze5jk42r7nVxfc3GIBvq0JnheNw9dTAmNBfgBVKBSNaPvo3NymsK86MrVkyW
                      |cf4YbO6qe6/Yb7z2VJYFzgUGypriOmgNJsmPz/P/+D0fyAZJGbI9mf9CwwCdUce3
                      |/QIDAQAB
                      |-----END PUBLIC KEY-----
                      |""".stripMargin
  val mockRepoId = 8675309
  val mockTravisToken = "xZ-dkkuUBH7823CMfm3WeR"
}

class MockTravisEndpoints extends TravisEndpoints {

  import MockTravisEndpoints._

  def getRepoKey(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): String = mockRepoKey

  def putHook(endpoint: TravisAPIEndpoint, headers: HttpHeaders, body: util.HashMap[String, Object]): Unit = Unit

  def postUsersSync(endpoint: TravisAPIEndpoint, headers: HttpHeaders): Unit = Unit

  def getRepo(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int = mockRepoId

  def postAuthGitHub(endpoint: TravisAPIEndpoint, githubToken: GitHubToken): TravisToken = TravisToken(mockTravisToken)

  def postRestartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, number: Int): Unit = Unit

  def postStartBuild(endpoint: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug,
                     message: String, envVars: Seq[String]): Unit = Unit

  def getRepoRetryingWithSync(api: TravisAPIEndpoint, headers: HttpHeaders, repoSlug: RepoSlug): Int =
    getRepo(api, headers, repoSlug)

}

