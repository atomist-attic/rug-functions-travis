package com.atomist.rug.functions.travis

import org.scalatest.FlatSpec

object FunctionResponseHelpers extends FlatSpec {

  def checkResponseMessage(msgOption: Option[String], expected: String): Unit = msgOption match {
    case Some(m) if m === expected =>
    case Some(x) => fail(s"response message ($x) did not match expected ($expected)")
    case None => fail(s"response did not contain a message")
  }

}
