#!/bin/bash

IMAGE=wavebeans/jupyter-wave

if [ -z "$VERSION" ]; then
  VERSION=$(cat ../gradle.properties | grep version | sed -E "s/[^=]+=//")
fi
WAVEBEANS_VERSION=$(cat ../gradle.properties | grep wavebeansVersion  | sed -E "s/[^=]+=//")

echo "VERSION: '$VERSION'"
echo "WAVEBEANS_VERSION: '$WAVEBEANS_VERSION'"

cat jupyter-wave.json.tpl | \
  sed "s/\$VERSION/$VERSION/" | \
  sed "s/\$WAVEBEANS_VERSION/$WAVEBEANS_VERSION/" \
  > jupyter-wave.json

TAG=$IMAGE:$VERSION

docker build -t $TAG .

rm jupyter-wave.json

if [ "$1" == "andRun" ]; then
  DOCKER_BUILD_DIR=$(pwd)

  # prepare artifacts
  cd ../
  ./gradlew clean publishToMavenLocal

  cd $DOCKER_BUILD_DIR || exit

  # make sure wavebeans artifacts are fresh during the run
  rm -rf ./ivy_cache/io.wavebeans*

  # run
  docker run -it \
    -p 8888:8888 \
    -p 2844:2844 \
    -e DROPBOX_CLIENT_IDENTIFIER=${DROPBOX_CLIENT_IDENTIFIER} \
    -e DROPBOX_ACCESS_TOKEN=${DROPBOX_ACCESS_TOKEN} \
    -e HTTP_PORT=2844 \
    -v "$(pwd)"/notebooks:/home/jovyan/work \
    -v ${HOME}/.m2:/home/jovyan/maven-local \
    -v "$(pwd)"/ivy_cache:/home/jovyan/.ivy2/cache \
    "${TAG}" \
    jupyter lab --NotebookApp.token=''
fi

if [ "$1" == "andPush" ]; then
    docker push $TAG
fi
