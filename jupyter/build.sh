#!/bin/bash

TAG=jupyter-wave

docker build -t $TAG .

if [ "$1" == "andRun" ]; then
  DOCKER_BUILD_DIR=$(pwd)

  # prepare artifacts
  cd ../
  ./gradlew clean publishToMavenLocal

  cd $DOCKER_BUILD_DIR || exit

  # run
  docker run -it \
    -p 8888:8888 \
    -p 12345:12345 \
    -v "$(pwd)"/notebooks:/home/jovyan/work \
    -v ${HOME}/.m2:/home/jovyan/maven-local \
    -v "$(pwd)"/ivy_cache:/home/jovyan/.ivy2/cache \
    $TAG

fi
