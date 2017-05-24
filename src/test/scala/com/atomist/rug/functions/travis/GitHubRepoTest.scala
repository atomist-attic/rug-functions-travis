package com.atomist.rug.functions.travis

import org.scalatest.{FlatSpec, Matchers}

class GitHubRepoTest extends FlatSpec with Matchers {

  val mgh = new MockGitHubRepo
  val slug = RepoSlug("noone", "nothing")

  it should "return the org endpoint for a public repo" in {
    assert(mgh.travisEndpoint(slug, GitHubToken("public")) === TravisOrgEndpoint)
  }

  it should "return the com endpoint for a private repo" in {
    assert(mgh.travisEndpoint(slug, GitHubToken("private")) === TravisComEndpoint)
  }

}
