package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import org.scalatest.{FlatSpec, Matchers}

class BuildRugTest extends FlatSpec with Matchers {

  import FunctionResponseHelpers._

  it should "return failure when starting a build fails" in {
    val failBuild = new BuildRug(new FailTravisEndpoints, new MockGitHubRepo)
    val fr = failBuild.build(RepoSlug("noone", "nothing"), "2.7.1828", "TK421", "master", TravisToken("nottravistoken"),
      "https://company.jfrog.io/company", "124kt", "nomaventoken", GitHubToken("notusertoken"))
    assert(fr.status === Status.Failure)
    val expected = "Failed to start build for noone/nothing on Travis CI"
    checkResponseMessage(fr.msg, expected)
  }

}
