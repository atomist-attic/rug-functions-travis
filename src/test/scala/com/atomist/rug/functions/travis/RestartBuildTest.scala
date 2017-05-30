package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import org.scalatest.{FlatSpec, Matchers}

class RestartBuildTest extends FlatSpec with Matchers {

  import FunctionResponseHelpers._

  val mockTravisEndpoints: TravisEndpoints = new MockTravisEndpoints
  val repoHook = RestartBuild(mockTravisEndpoints, new MockGitHubRepo)
  val repoSlug = RepoSlug("noone", "nothing")
  val buildId = "8675309"
  val ght = GitHubToken("notatoken")

  it should "enable a repo" in {
    val fr = repoHook.tryRestart(repoSlug, buildId, ght)
    assert(fr.status === Status.Success)
    val expected = s"Successfully restarted $repoSlug build `$buildId` on Travis CI"
    checkResponseMessage(fr.msg, expected)
  }

  val failTravisEndpoints: TravisEndpoints = new FailTravisEndpoints
  val failHook = RestartBuild(failTravisEndpoints, new MockGitHubRepo)

  it should "return failure when enabling a repo fails" in {
    val fr = failHook.tryRestart(repoSlug, buildId, ght)
    assert(fr.status === Status.Failure)
    val expected = s"Failed to restart $repoSlug build `$buildId` on Travis CI"
    checkResponseMessage(fr.msg, expected)
  }

}
