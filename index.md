![Coverage](https://epic-64.github.io/my-cool-website/coverage/coverage-badge.svg)

[View Coverage Report](https://epic-64.github.io/my-cool-website/coverage/index.html)

[Source Code](https://github.com/epic-64/my-cool-website)

## Auto recompile on file change
- open new terminal
- `sbt ~reStart`

## Run performance test
```bash
ab -n 10000 -c 10 http://localhost:8080/hello/yourname
```

## Set the correct git user for the project
```
git config user.name "github username"
git config user.email "github email"
```
Afterward, when pushing, it should use the correct user.

## Setup git privacy
install https://github.com/EMPRI-DEVOPS/git-privacy
```bash
git-privacy init
```
```bash
git config privacy.pattern hms
```

## Add gitignore
This should cover 95% of the stuff you don't want to commit.
```
.idea
/project/target/
/project/project/target/
/target/
```

## Add project/plugins.sbt
```scala
addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.2.2")
```

## Add scalafmt
My config looks like this
```hocon
version = 3.8.2
runner.dialect = scala3

maxColumn = 120
align.preset = most
danglingParentheses.preset = true
rewrite.rules = [RedundantBraces, RedundantParens]
binPack.parentConstructors = false
trailingCommas = keep
```

## Coverage Pages
Go to the project settings and set the pages to the `gh-pages` branch.
https://github.com/epic-64/my-cool-website/settings/pages
