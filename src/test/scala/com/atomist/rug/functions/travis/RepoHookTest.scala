package com.atomist.rug.functions.travis

import com.atomist.rug.spi.Handlers.Status
import org.scalatest.{FlatSpec, Matchers}

class RepoHookTest extends FlatSpec with Matchers {

  import FunctionResponseHelpers._

  val mockTravisEndpoints: TravisEndpoints = new MockTravisEndpoints
  val repoHook = new RepoHook(mockTravisEndpoints)

  it should "enable a repo" in {
    val fr = repoHook.tryRepoHook(true, "noone", "nothing", "notoken", ".org")
    assert(fr.status === Status.Success)
    val expected = "Successfully enabled Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

  it should "disable a repo" in {
    val fr = repoHook.tryRepoHook(false, "noone", "nothing", "notoken", ".org")
    assert(fr.status === Status.Success)
    val expected = "Successfully disabled Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

  val failTravisEndpoints: TravisEndpoints = new FailTravisEndpoints
  val failHook = new RepoHook(failTravisEndpoints)

  it should "return failure when enabling a repo fails" in {
    val fr = failHook.tryRepoHook(true, "noone", "nothing", "notoken", ".org")
    assert(fr.status === Status.Failure)
    val expected = "Failed to enable Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

  it should "return failure when disabling a repo fails" in {
    val fr = failHook.tryRepoHook(false, "noone", "nothing", "notoken", ".org")
    assert(fr.status === Status.Failure)
    val expected = "Failed to disable Travis CI for noone/nothing"
    checkResponseMessage(fr.msg, expected)
  }

}
