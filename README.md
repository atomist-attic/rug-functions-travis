# Atomist 'rug-functions-travis'

[![Build Status](https://travis-ci.org/atomist/rug-functions-travis.svg?branch=master)](https://travis-ci.org/atomist/rug-functions-travis)
[![Slack Status](https://join.atomist.com/badge.svg)](https://join.atomist.com)

Rug functions that hit the [Travis CI][travis-ci] API.  Currently
contains the following Rug functions:

-   `restart-travis-build(org, buildId, token)`
-   `travis-build-rug(owner, repo, version, teamId, gitRef, travisToken, mavenBaseUrl, mavenUser, mavenToken, token)`
-   `travis-enable-repo(owner, repo, token)`
-   `travis-disable-repo(owner, repo, token)`
-   `travis-encrypt(owner, repo, content, token)`

[travis-ci]: https://travis-ci.org/

These functions are intended to be called from Rug event and command
handlers, which are usually written in [TypeScript][ts].  Here is an
example of how you would add calling the `travis-enable-repo` Rug
function to enable builds on Travis CI for the repo, adding it to the
plan returned by a command handler.

```typescript
plan.add(PlanUtils.execute("travis-enable-repo", {
    repo: "your-public-repo",
    owner: "your-github-org"
}));
```

[ts]: https://www.typescriptlang.org/

Please see the [Atomist Documentation][docs] for more information.

[docs]: http://docs.atomist.com/

## Authentication

Authenticating against the Travis API requires a GitHub token with
proper scopes.  The Travis CI public repository (`.org`) and private
repository (`.com`) endpoints require different scopes.  You can
always get the current list of scopes for each endpoint directly from
Travis CI by running the command below, changing `ENDPOINT` to `com`
for private repositories or `org` for public repositories.

```
$ curl -s -H 'Content-Type: application/json' -H 'User-Agent: CurlClient/1.0.0' -H 'Accept: application/vnd.travis-ci.2+json' https://api.travis-ci.ENDPOINT/config | jq .config.github.scopes
```

All of the Travis Rug functions require a GitHub token, accessed
via [Rug Secrets][secrets], with the "repo", "read:org", and
"user:email" scopes, which is a union of the scopes required by the
`.org` and `.com` endpoints.  The token *may* need to be a from a
GitHub user who is an owner of the repository.  If the owner of the
repository is a GitHub organization, this means the token must be from
a user in the Owner group.

[secrets]: http://docs.atomist.com/user-guide/rug/secrets/ (Rug Secrets)

## Support

General support questions should be discussed in the `#support`
channel on our community Slack team
at [atomist-community.slack.com][slack].

If you find a problem, please create an [issue][].

[issue]: https://github.com/atomist/rug-functions-travis/issues

## Building

To build, test, and install:

```
$ mvn install
```

## Releasing

To create a new release of the project, simply push a tag of the form
`M.N.P` where `M`, `N`, and `P` are integers that form the next
appropriate [semantic version][semver] for release.  For example:

[semver]: http://semver.org

```
$ git tag -a 1.2.3
```

The Travis CI build (see badge at the top of this page) will
automatically create a GitHub release using the tag name for the
release and the comment provided on the annotated tag as the contents
of the release notes.  It will also automatically upload the needed
artifacts.

---
Created by [Atomist][atomist].
Need Help?  [Join our Slack team][slack].

[atomist]: https://www.atomist.com/
[slack]: https://join.atomist.com/
