package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import org.scalatest.{FlatSpec, Matchers}

class RepoHookTest extends FlatSpec with Matchers {

  import FunctionResponseHelpers._

  val mockTravisEndpoints: TravisEndpoints = new MockTravisEndpoints
  val repoHook = RepoHook(mockTravisEndpoints, new MockGitHubRepo)
  val repoSlug = RepoSlug("noone", "nothing")
  val ght = GitHubToken("notatoken")

  it should "enable a repo" in {
    val fr = repoHook.tryRepoHook(true, repoSlug, ght)
    assert(fr.status === Status.Success)
    val expected = "Successfully enabled Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

  it should "disable a repo" in {
    val fr = repoHook.tryRepoHook(false, repoSlug, ght)
    assert(fr.status === Status.Success)
    val expected = "Successfully disabled Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

  val failTravisEndpoints: TravisEndpoints = new FailTravisEndpoints
  val failHook = RepoHook(failTravisEndpoints, new MockGitHubRepo)

  it should "return failure when enabling a repo fails" in {
    val fr = failHook.tryRepoHook(true, repoSlug, ght)
    assert(fr.status === Status.Failure)
    val expected = "Failed to enable Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

  it should "return failure when disabling a repo fails" in {
    val fr = failHook.tryRepoHook(false, repoSlug, ght)
    assert(fr.status === Status.Failure)
    val expected = "Failed to disable Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

}
