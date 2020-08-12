# Jupyter Wave

Jupyter + WaveBeans plugins and integration

Based on:

* [kotlin-jupyter](https://github.com/Kotlin/kotlin-jupyter)
* Kotlin 1.4 version of [WaveBeans](https://github.com/WaveBeans/wavebeans/pull/72)

Project status: underlying projects are in early alpha stage, moreover project bases on non-stable version of Kotlin and experimental kotlin Scripting API, hence the extension should also be considered experimental as well.

## Jupyter Plugin

The Kotlin Jupyter plugin supports adding libraries. This is how Jupyter-Wave runs, it is done via adding file `jupyter/jupyter-wave.json`. The file itself is a template which expects the library version and WaveBeans version specified during build time. The versions are specified in `gradle.properties` file.

Overall follow [kotlin-jupyter](https://github.com/Kotlin/kotlin-jupyter) documentation how to add the library to get some sense.

## Running via Docker

The recommended way to run Jupyter instance is via Docker. In `jupyter/` directory you can find `Dockerfile` which build the image. 

There is a `build.sh` script that automates the build and run (via `andRun` parameter) of the docker image. Default run behaviour start everything that needs to make Jupyter accessible on `http://localhost:8888`.

In order to run by yourself use that commands as a base:

```bash
  cd jupyter/

  # prepare the library descriptor, assuming VERSION and WAVEBEANS_VERSION are populated with correct version in maven repos
  cat jupyter-wave.json.tpl | \
      sed "s/\$VERSION/$VERSION/" | \
      sed "s/\$WAVEBEANS_VERSION/$WAVEBEANS_VERSION/" \
      > jupyter-wave.json

  # build image
  docker build -t jupyter-wave .

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
    "jupyter-wave" \
    jupyter lab --NotebookApp.token=''
```

## Runtime Configuration

The evaluator can be parameterized via environment variables:

1. To start a streaming capabilities for preview functionality you need to specify the http port via `HTTP_PORT` environment variable, and do not forget to expose it if running docker or somewhere else. It'll start the [HTTP Service](https://wavebeans.io/docs/http/) with streaming capabilities.
    * If the HTTP service needs to be accessible on a different host, protocol, and/or port, specify `ADVERTISED_HTTP_HOST`, `ADVERTISED_HTTP_PROTOCOL` and `ADVERTISED_HTTP_PORT` environment variables accordingly. The host is defaulted to `localhost`, port to `HTTP_PORT` value, and protocol to `http`. 
2. Pre-populate DropBox configuration. DropBox File Driver is configured automatically if `DROPBOX_CLIENT_IDENTIFIER` and `DROPBOX_ACCESS_TOKEN` is specified.
3. To start up [management server](#management-server) specify `MANAGEMENT_SERVER_PORT` environment variable.

## API extension

On top of [WaveBeans API](https://wavebeans.io/docs/api/) there are a few functions available for convenient use within Jupyter notebooks.

### Preview

Preview allows you to listen to sample stream, it is available for any `BeanStream<Sample>`:

```kotlin
wave("dropbox:///song.wav").preview()
```

Certain things to keep in mind:

* Preview is available only for limited streams, and by default it limits any stream with 10 minutes. You can change it by specifying `maxLength` parameter if you need shorter or longer, also you may use [`trim()`](https://wavebeans.io/docs/api/operations/trim-operation.html) operation yourself, but if you need to do longer then default 10 minutes you would need to specify `maxLength` anyway:

```kotlin
440.sine().preview(maxLength = 1.s) // limit to 1 second
440.sine().trim(1000).preview() // already limited by 1 second
440.sine().trim(15, TimeUnit.MINUTES).preview(maxLength = 20.m) // need to extend the preview limit also
```

* The stream is evaluated with default frequency 44100Hz, to change that specify custom `sampleRate` parameter:

```kotlin
440.sine().preview(sampleRate = 22050.0f)
```

### Plot

You may plot out the waveform or any `FiniteStream<Sample>` with `lets_plot` kotlin library.

First of all you need to make sure the library is connected to the notebook:

```text
%use lets-plot
```

Then you may use `.plot()` function, but before makre sure you trimmed it:

```
440.sine().trim(100).plot()
```

Plot function allows you to specify the sample rate via `sampleRate` parameter, by default it is 44100Hz:

```kotlin
440.sine().trim(100).plot(sampleRate = 22050.0f)
```

## Management server

Management server allows you to change the configuration of the running Wave instance via REST API. To start the server specify the `MANAGEMENT_SERVER_PORT` where it is going to be accessible. Once it is started you read or write values.

* To read the value perform the `GET` request to `http://server:port/config/VARIABLE_NAME`. The `VARIABLE_NAME` can be one of the predefined ones, or your own. The value is returned as a body, if the variable is not found 404 code is returned.
* To write the value perform the `POST` request to `http://server:port/config/VARIABLE_NAME`. The `VARIABLE_NAME` can be one of the predefined ones, or your own. The value shoudl be specified in the body of request.