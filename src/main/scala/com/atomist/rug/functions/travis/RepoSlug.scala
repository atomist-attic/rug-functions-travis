package com.atomist.rug.functions.travis

/**
  * A simple type to represent a full GitHub repository name.
  *
  * @param owner  user or organization owning this repository
  * @param name   simple name of repository
  */
case class RepoSlug(owner: String, name: String) {

  override def toString: String = s"$owner/$name"

}
