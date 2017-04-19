package com.atomist.rug.functions.travis

import java.util.Collections

import com.atomist.rug.InvalidRugParameterPatternException
import org.scalatest.{FlatSpec, Matchers}

class RealTravisEndpointsTest extends FlatSpec with Matchers {

  it should "return cached token" in {
    val t: String = "notarealtravistoken"
    val rte: RealTravisEndpoints = new RealTravisEndpoints
    rte.travisTokens = Collections.singletonMap("doesnotmatter", "notarealtravistoken")
    val api: TravisAPIEndpoint = TravisOrgEndpoint
    assert(rte.postAuthGitHub(api, "doesnotmatter") === t)
  }
}
