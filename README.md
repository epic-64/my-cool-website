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
