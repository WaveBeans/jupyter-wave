#!/bin/bash

# Script to pushish Jupyer Wave changes
# Usage:
# export BINTRAY_USER=<user on bintray with publish rights>
# export BINTRAY_KEY=<the key of that user>
# export DOCKER_USER=<docker user with publish rights>
# export DOCKER_PWD=<the password of this user>
# ./publish.sh <the-version>

function check_exit_code() {
  TARGET=$1
  # shellcheck disable=SC2181
  if [ "$?" -ne 0 ]; then
    E=$?
    echo "[ERROR] $TARGET returned $E exit code"
    exit $E
  fi

}

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "Please specify version as a parameter"
  exit 1
fi
if [ -z "$BINTRAY_USER" ]; then
  echo "Please specify BINTRAY_USER as env var"
  exit 2
fi
if [ -z "$BINTRAY_KEY" ]; then
  echo "Please specify BINTRAY_KEY as env var"
  exit 3
fi

WORK_DIR=$(pwd)
PROJECT_ROOT="$WORK_DIR/.."
GRADLE_PROP_FILE="${PROJECT_ROOT}/gradle.properties"


cd "$PROJECT_ROOT" || exit

################
# update gradle.prooeries
################
NEW_PROPS=$(cat "$GRADLE_PROP_FILE" | sed -E "s/^version\=.+$/version=$VERSION/")
echo "$NEW_PROPS" > "$GRADLE_PROP_FILE"
git add $GRADLE_PROP_FILE

################
# publish kotlin artifacts
################
./gradlew clean build bintrayUpload -Pversion=$VERSION -Pbintray.user=$BINTRAY_USER -Pbintray.key=$BINTRAY_KEY
check_exit_code "publish kotlin artifacts"

cd "$WORK_DIR" || exit

################
# create kotlin-jupyter library descriptor
################
DESCRIPTOR_FILE="$WORK_DIR/stable/jupyter-wave.json"
./tpl.sh $DESCRIPTOR_FILE "$VERSION"
check_exit_code "create kotlin-jupyter library descriptor"
git add $DESCRIPTOR_FILE

################
# publish docker image
################
docker logout > /dev/null
docker login --username $DOCKER_USER --password $DOCKER_PWD 2> /dev/null
./build.sh andPush

################
# update README file
################
README_FILE="$PROJECT_ROOT/README.md"

IMAGE_HASH=$(
curl https://hub.docker.com/v2/repositories/wavebeans/jupyter-wave/tags/?page_size=25\&page=1\&ordering=last_updated \
  | jq -r ".results[] | select(.name==\"$VERSION\").images[0].digest" \
  | sed "s/sha256://"
)

cp $README_FILE $README_FILE~

cat $README_FILE~ \
  | sed -E "s/[0-9]+\.[0-9]+\.[0-9]+(-[\a-z0-9]+)?/$VERSION/g" \
  | sed -E "s/sha256-[0-9a-z]+/sha256-$IMAGE_HASH/" \
  > $README_FILE

rm $README_FILE~
git add $README_FILE