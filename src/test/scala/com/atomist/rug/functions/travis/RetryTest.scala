package com.atomist.rug.functions.travis

import org.scalatest.{FlatSpec, Matchers}

class RetryTest extends FlatSpec with Matchers {

  import Retry._

  it should "succeed when things succeed" in {
    val v = retry("test success", 2) { 1 }
    assert(v === 1)
  }

  it should "take some time to retry when it fails" in {
    val startTime = System.currentTimeMillis
    try {
      retry("test fail", 4, 10L) {
        throw new Exception("testing failed retry")
      }
    } catch {
      case _: Exception =>
    }
    val endTime = System.currentTimeMillis
    assert(startTime + 150 < endTime)
  }

}
