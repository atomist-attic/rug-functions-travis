package com.atomist.rug.functions.travis

import com.atomist.rug.InvalidRugParameterPatternException
import org.scalatest.{FlatSpec, Matchers}

class TravisAPIEndpointTest extends FlatSpec with Matchers {

  import TravisAPIEndpoint._

  "stringToTravisEndpoint" should "accept org" in {
    assert(stringToTravisEndpoint("org") === TravisOrgEndpoint)
  }

  it should "accept .org" in {
    assert(stringToTravisEndpoint(".org") === TravisOrgEndpoint)
  }

  it should "accept com" in {
    assert(stringToTravisEndpoint("com") === TravisComEndpoint)
  }

  it should "accept .com" in {
    assert(stringToTravisEndpoint(".com") === TravisComEndpoint)
  }

  it should "throw an exception if not given a valid API type" in {
    an[InvalidRugParameterPatternException] should be thrownBy stringToTravisEndpoint(".blah")
  }

}
