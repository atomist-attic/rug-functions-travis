package com.atomist.rug.functions.travis

import java.util.Collections

import org.scalatest.{FlatSpec, Matchers}

class RealTravisEndpointsTest extends FlatSpec with Matchers {

  "postAuthGithub" should "return cached token" in {
    val t = TravisToken("notarealtravistoken")
    val rte: RealTravisEndpoints = new RealTravisEndpoints
    val ght = GitHubToken("doesnotmatter")
    rte.travisTokens = Collections.singletonMap(ght, t)
    val api: TravisAPIEndpoint = TravisOrgEndpoint
    assert(rte.postAuthGitHub(api, ght) === t)
  }

}
