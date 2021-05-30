FROM jupyter/minimal-notebook:b90cce83f37b

USER root

# install conda
# based on https://github.com/conda/conda-docker/blob/master/miniconda3/debian/Dockerfile
RUN apt-get -qq update && apt-get -qq -y install curl bzip2 \
    && curl -sSL https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh -o /tmp/miniconda.sh \
    && bash /tmp/miniconda.sh -bfp /usr/local \
    && rm -rf /tmp/miniconda.sh \
    && conda install -y python=3 \
    && conda update conda \
    && apt-get -qq -y remove curl bzip2 \
    && apt-get -qq -y autoremove \
    && apt-get autoclean \
    && rm -rf /var/lib/apt/lists/* /var/log/dpkg.log

# Install Jupyter lab extension

RUN conda install -c conda-forge jupyterlab

# Install OpenJDK-8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# Install kotlin kernel
RUN conda install -c jetbrains kotlin-jupyter-kernel=0.10.0.40

USER jovyan

# build from scratch
#RUN mkdir -p /home/jovyan/build && \
#    cd /home/jovyan/build && \
#    git clone https://github.com/Kotlin/kotlin-jupyter.git && \
#    cd /home/jovyan/build/kotlin-jupyter && \
#    git fetch --depth 1 origin d486046b068a9e1eba5e3ebf9fe951c5ada8be98 && \
#    ./gradlew --no-daemon --info install && \
#    rm -r /home/jovyan/build && \
#    rm -r /home/jovyan/.gradle

COPY jupyter-wave.json /opt/conda/lib/python3.8/site-packages/run_kotlin_kernel/libraries/

#ENTRYPOINT ls -laR ~/maven-local/repository/io/wavebeans/jupyter-wave/0.3.0/