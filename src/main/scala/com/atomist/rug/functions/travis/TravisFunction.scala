package com.atomist.rug.functions.travis

import com.atomist.rug.runtime.Rug

/**
  * Base trait for all Travis Rug functions providing an implementation
  * of TravisEndpoints
  */
trait TravisFunction extends Rug {

  lazy val travisEndpoints: TravisEndpoints = new RealTravisEndpoints

}

object TravisFunction {

  final val githubTokenPath = "github://user_token?scopes=repo,read:org,user:email"

}
