## Set the correct git user for the project
```
git config user.name "github username"
git config user.email "github email"
```
Afterwards when pushing it should use the correct user.

## Setup git privacy
https://github.com/EMPRI-DEVOPS/git-privacy
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