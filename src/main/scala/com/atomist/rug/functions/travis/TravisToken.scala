package com.atomist.rug.functions.travis

/**
  * Simple class to hold a Travis API token.
  *
  * @param token Travis API token
  */
case class TravisToken(token: String) {

  override def toString: String = token

}
