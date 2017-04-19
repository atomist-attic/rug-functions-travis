package com.atomist.rug.functions.travis

import com.atomist.rug.InvalidRugParameterPatternException

trait TravisAPIEndpoint {

  def tld: String

}

object TravisAPIEndpoint {

  def stringToTravisEndpoint(ep: String): TravisAPIEndpoint = ep match {
    case ".org" | "org" => TravisOrgEndpoint
    case ".com" | "com" => TravisComEndpoint
    case _ => throw new InvalidRugParameterPatternException("Travis CI endpoint must be 'org' or 'com'")
  }

  def isPublic(ep: String): Boolean = ep match {
    case ".org" | "org" => true
    case ".com" | "com" => false
    case _ => throw new InvalidRugParameterPatternException("Travis CI endpoint must be 'org' or 'com'")
  }

}

object TravisOrgEndpoint extends TravisAPIEndpoint {
  val tld: String = "org"
}

object TravisComEndpoint extends TravisAPIEndpoint {
  val tld: String = "com"
}
