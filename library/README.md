# Deploy

Run these command from the root of the project

## Local Maven

```
# purge the local repo if you want
rm -rf ~/.m2/repository/com/factual/engine/analytics/analytics-engine/

./gradlew :library:clean :library:install
```

## Internal Factual Nexus

Deploy snapshots internally. [This](https://wiki.corp.factual.com/display/ENG/Internal+Maven+Proxy+Repository) wiki page has the credentials.

```
export DEPLOY_REPO_URL=http://maven.corp.factual.com/nexus/content/repositories/snapshots
export DEPLOY_REPO_USER=<USER NAME HERE>
export DEPLOY_REPO_PASS=<PASSWORD HERE>
./gradlew :library:clean :library:uploadArchives
```

## Bintray

This is for releases only.

**Note**: Don't forget to log in to Bintray and publish the upload!

```
export DEPLOY_REPO_URL="https://api.bintray.com/maven/factual/maven/segment-analytics-factual-engine;publish=1"
export DEPLOY_REPO_USER=<BINTRAY USER>
export DEPLOY_REPO_PASS=<BINTRAY API KEY>
./gradlew :library:clean :library:uploadArchives
```