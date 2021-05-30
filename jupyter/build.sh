#!/bin/bash

set -e

IMAGE=wavebeans/jupyter-wave
JUPYTER_PORT=8999
WB_HTTP_PORT=2844
WB_MANAGEMENT_PORT=2845

if [ -z "$VERSION" ]; then
  VERSION=$(cat ../gradle.properties | grep version | sed -E "s/[^=]+=//")
  if [[ "$VERSION" == *SNAPSHOT ]]; then
    NOW=$(date +%s)
    VERSION=$(echo $VERSION | sed -E "s/SNAPSHOT/$NOW/")
  fi
fi

ADDITIONAL_REPOSITORIES=""
if [ "$1" == "andRun" ]; then
  ADDITIONAL_REPOSITORIES='\"file:///home/jovyan/maven-local/repository\",'
fi
./tpl.sh jupyter-wave.json "$VERSION" "$ADDITIONAL_REPOSITORIES"

TAG=$IMAGE:$VERSION
TAG_LATEST=$IMAGE:latest

docker build -t $TAG -t $TAG_LATEST .

rm jupyter-wave.json

if [ "$1" == "andRun" ]; then
  DOCKER_BUILD_DIR=$(pwd)

  # prepare artifacts
  cd ../
  ./gradlew clean publishToMavenLocal --info -Pversion=$VERSION -Pskip.signing

  cd $DOCKER_BUILD_DIR || exit

  # make sure wavebeans artifacts are fresh during the run
  rm -rf ./ivy_cache/io.wavebeans.*
  rm -rf ./ivy_cache/io.wavebeans

  # run
  docker run -it \
    -p $JUPYTER_PORT:8888 \
    -p $WB_HTTP_PORT:$WB_HTTP_PORT \
    -p $WB_MANAGEMENT_PORT:$WB_MANAGEMENT_PORT \
    -e DROPBOX_CLIENT_IDENTIFIER=${DROPBOX_CLIENT_IDENTIFIER} \
    -e DROPBOX_ACCESS_TOKEN=${DROPBOX_ACCESS_TOKEN} \
    -e HTTP_PORT=$WB_HTTP_PORT \
    -e MANAGEMENT_SERVER_PORT=$WB_MANAGEMENT_PORT \
    -v "$(pwd)"/notebooks:/home/jovyan/work \
    -v "${HOME}"/.m2:/home/jovyan/maven-local \
    -v "$(pwd)"/ivy_cache:/home/jovyan/.ivy2/cache \
    "${TAG}" \
    jupyter lab --NotebookApp.token=''
fi

if [ "$1" == "andPush" ]; then
    docker push $TAG
    docker push $TAG_LATEST
fi
