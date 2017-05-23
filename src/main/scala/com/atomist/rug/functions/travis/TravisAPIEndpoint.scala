package com.atomist.rug.functions.travis

import com.atomist.rug.InvalidRugParameterPatternException

sealed trait TravisAPIEndpoint {

  def tld: String

}

object TravisAPIEndpoint {

  def stringToTravisEndpoint(ep: String): TravisAPIEndpoint = ep match {
    case ".org" | "org" | "public" => TravisOrgEndpoint
    case ".com" | "com" | "private" => TravisComEndpoint
    case _ => throw new InvalidRugParameterPatternException("Travis CI endpoint must be 'org', 'public', 'com', or 'private'")
  }

  def isPublic(ep: String): Boolean = stringToTravisEndpoint(ep) == TravisOrgEndpoint

}

case object TravisOrgEndpoint extends TravisAPIEndpoint {
  val tld: String = "org"
}

case object TravisComEndpoint extends TravisAPIEndpoint {
  val tld: String = "com"
}
