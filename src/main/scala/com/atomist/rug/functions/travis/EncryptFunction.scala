package com.atomist.rug.functions.travis

import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse}
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}

/**
  *  Use Travis API to encrypt strings using the repository public key
  */
class EncryptFunction
  extends AnnotatedRugFunction
    with TravisFunction {

  /** Encrypts a value using Travis CI repo public key
    *
    * @param owner   GitHub owner, i.e., user or organization, of the repo to enable
    * @param repo    name of the repo to enable
    * @param org     Travis CI ".com" or ".org" endpoint
    * @param content content to encrypt
    * @param token   GitHub token with "repo" scope for `owner`/`repo`
    * @return `content` encrypted using the Travis CI repo public key
    */
  @RugFunction(name = "travis-encrypt", description = "Encrypts a value using Travis CI repo public key",
    tags = Array(new Tag(name = "travis-ci"), new Tag(name = "ci")))
  def encrypt(@Parameter(name = "owner") owner: String,
              @Parameter(name = "repo") repo: String,
              @Parameter(name = "org") org: String,
              @Parameter(name = "content") content: String,
              @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String
             ): FunctionResponse = Encrypt(travisEndpoints).tryEncrypt(owner, repo, org, content, token)

}
