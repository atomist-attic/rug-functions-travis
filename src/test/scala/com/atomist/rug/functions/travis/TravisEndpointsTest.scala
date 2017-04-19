package com.atomist.rug.functions.travis

import java.util

import org.scalatest.{FlatSpec, Matchers}
import org.springframework.http.HttpHeaders

import scala.util.matching.Regex

class TravisEndpointsTest extends FlatSpec with Matchers {

  private def checkHeader(k: String, v: String): Unit = {
    val hdrs = TravisEndpoints.headers.get(k)
    hdrs.size() === 1
    hdrs.get(0) === v
  }

  private def matchHeader(k: String, regex: Regex): Unit = {
    val hdrs = TravisEndpoints.headers.get(k)
    hdrs.size() === 1
    hdrs.get(0) match {
      case regex() =>
      case x => fail(s"header $k failed to match $regex: $x")
    }
  }

  "headers" should "return headers" in {
    TravisEndpoints.headers.size() > 0
  }

  it should "specify travis in user agent header" in {
    matchHeader("User-Agent", """.*\bTravis\b.*""".r)
  }

  it should "specify a content type of application/json" in {
    checkHeader("Content-Type", "application/json")
  }

  it should "specify it accepts travis return type" in {
    matchHeader("Accept", """.*\btravis\b.*\bjson\b.*""".r)
  }

  "authHeaders" should "return more headers" in {
    TravisEndpoints.headers.size() + 1 === TravisEndpoints.authHeaders("phonytoken").size()
  }

  it should "include the token in the authorization header" in {
    val phonyToken = "notarealtokenforanything"
    val authHeaders = TravisEndpoints.authHeaders(phonyToken).get("Authorization")
    authHeaders.size() === 1
    val tokenRE = s".*\\b$phonyToken\\b.*".r
    authHeaders.get(0) match {
      case tokenRE() =>
      case x => fail(s"authorization header failed to match provided token: $x")
    }
  }
}
