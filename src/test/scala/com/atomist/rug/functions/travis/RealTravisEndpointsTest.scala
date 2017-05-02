package com.atomist.rug.functions.travis

import java.util.Collections

import org.scalatest.{FlatSpec, Matchers}

class RealTravisEndpointsTest extends FlatSpec with Matchers {

  "postAuthGithub" should "return cached token" in {
    val t: String = "notarealtravistoken"
    val rte: RealTravisEndpoints = new RealTravisEndpoints
    rte.travisTokens = Collections.singletonMap("doesnotmatter", "notarealtravistoken")
    val api: TravisAPIEndpoint = TravisOrgEndpoint
    assert(rte.postAuthGitHub(api, "doesnotmatter") === t)
  }

  val rte = new RealTravisEndpoints

  "retry" should "succeed when things succeed" in {
    val v = rte.retry("test success", 2) { 1 }
    assert(v === 1)
  }

  it should "take some time to retry when it fails" in {
    val startTime = System.currentTimeMillis
    try {
      rte.retry("test fail", 4, 10L) {
        throw new Exception("testing failed retry")
      }
    } catch {
      case _: Exception =>
    }
    val endTime = System.currentTimeMillis
    assert(startTime + 150 < endTime)
  }
}
