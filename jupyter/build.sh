TAG=jupyter-wave

docker build -t $TAG .

if [ "$1" == "andRun" ]; then
  MAVEN_REPO_VOLUME="-v ${HOME}/.m2:/home/jovyan/maven-local"
  
  docker run -it \
    -p 8888:8888 \
    -p 12345:12345 \
    -v "$(pwd)"/notebooks:/home/jovyan/work ${MAVEN_REPO_VOLUME} \
    $TAG
fi