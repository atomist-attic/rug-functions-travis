package com.atomist.rug.functions.travis

/**
  * Simple class to hold a GitHub personal access token.
  *
  * @param token GitHub personal access token
  */
case class GitHubToken(token: String) {

  override def toString: String = token

}
