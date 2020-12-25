# Jupyter Wave

[ ![Download](https://api.bintray.com/packages/wavebeans/wavebeans/wavebeans/images/download.svg?version=0.3.0-fc3c6cb7) ](https://bintray.com/wavebeans/wavebeans/wavebeans/0.3.0-fc3c6cb7/link)
[ ![Docker Image](https://img.shields.io/docker/v/wavebeans/jupyter-wave/0.3.0-fc3c6cb7?label=Docker&logo=docker) ](https://hub.docker.com/layers/127231740/wavebeans/jupyter-wave/0.3.0-fc3c6cb7/images/sha256-dde0dfd74eb788a9b2d64d5e154716fd3805ab9cc3941c6c996d0f33441fef2f?context=explore)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Jupyter Plugin](#jupyter-plugin)
- [Running via Docker](#running-via-docker)
- [Runtime Configuration](#runtime-configuration)
- [API extension](#api-extension)
  - [Preview](#preview)
  - [Plot](#plot)
    - [Samples](#samples)
    - [FftSample](#fftsample)
  - [Data Frames](#data-frames)
- [Management server](#management-server)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

Jupyter + WaveBeans plugins and integration. It is based on:

* [kotlin-jupyter](https://github.com/Kotlin/kotlin-jupyter)
* [0.3.0-0709bae9](https://github.com/WaveBeans/wavebeans/tree/0709bae9) version of [WaveBeans](https://wavebeans.io)

Project status: underlying projects are in early alpha and beta stages, the extension should also be considered experimental at this point as well.

## Jupyter Plugin

The Kotlin Jupyter plugin supports adding libraries. This is how Jupyter-Wave runs, it is done via adding file `jupyter/jupyter-wave.json`. The file itself is a template which expects the library version and WaveBeans version specified during build time. The versions are specified in `gradle.properties` file.

Overall follow [kotlin-jupyter](https://github.com/Kotlin/kotlin-jupyter) documentation how to add the library to get some sense.

## Running via Docker

The recommended way to run Jupyter instance is via Docker. In `jupyter/` directory you can find `Dockerfile` which builds the image. 

There is a `build.sh` script that automates the build and run (via `andRun` parameter) of the docker image. Default run behaviour start everything that needs to make Jupyter accessible on `http://localhost:8888`.

In order to run by yourself use that commands as a base:

```bash
  cd jupyter/

  # prepare the library descriptor, assuming VERSION and WAVEBEANS_VERSION are populated with correct version in maven repos (local or remote)
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
    -e MANAGEMENT_SERVER_PORT=2845 \
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

* Preview is available only for limited streams, and by default it limits any stream with 10 minutes. You can change it by specifying `maxLength` parameter if you need shorter or longer, also you may use [`trim()`](https://wavebeans.io/docs/api/operations/trim-operation.html) operation yourself, but if you need to do longer then default 10 minutes value, you would need to specify `maxLength` anyway:

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

There are builtin plotting abilities with [`lets_plot`](https://github.com/JetBrains/lets-plot-kotlin) library for some streams.

First of all you need to make sure the library is connected to the notebook:

```text
%use lets-plot
```

Then depending on the stream you may call `.plot()` function tuning the output. It returns the `lets_lot` object instance with layers depending on the stream you've called it on, and you may tune the plot further, or adding new layers, e.g. change the size: 
                                                                                    
```
440.sine()
    .trim(10)
    .plot() + ggsize(800, 600)
```

#### Samples

You may plot out the waveform of any stream of `Sample`s with `.plot()` function:

On finite streams (of type `FiniteStream<Sample>`) you can call it directly:

```
wave("file:///my.wav").plot()
```

On infinite streams (of type `BeanStream<Sample>`) you you need to make sure the stream is trimmed to some value either with `.trim()` function explicitly, or telling the plot to trim it by itself via specifying `length: TimeMeasure` parameter:

```kotlin
// explicitly trimmed stream
440.sine()
    .trim(10)
    .plot()

// or plot will trim the stream by 10ms itself
440.sine()
    .plot(10.ms)
```

![Plotting 440Hz sine](assets/440_sine_plot.png "Plotting 440Hz sine")

Plot function allows you to specify the sample rate via `sampleRate` parameter, by default it is 44100Hz:

```kotlin
440.sine().trim(100).plot(sampleRate = 22050.0f)
```

#### FftSample

You may plot stream of `FftSample`s as a heat map using `.plot()` function on explicitly trimmed stream or let plot do this by specifying `length: TimeMeasure`:

```kotlin
val fft = wave("dropbox:///2.wav")
    .window(1001, 501)
    .hamming()
    .fft(1024)

// trim explicitly
fft.trim(2000)
    .plot(freqCutOff = 0 to 4500)

// or let plot do this
fft.plot(2.s, freqCutOff = 0 to 4500)
```

![Plotting FFT of wave file](assets/wave_fft_in_motion_plot.png "Plotting FFT of wave file")

Method has following parameters:

* `length` as `TimeMeasure` - on an infinite stream allows you to trim the stream implicitly, i.e. `1000.ms`.
* `sampleRate` as `Float` - the sample rate to evaluate the stream with, by default `44100.0f`.
* `freqCutOff` as `Pair<Number, Number>` - the range of the frequencies to display, by default `0 to sampleRate / 2.0`.

You may also call `plotAt()` to render FFT at specific time moment. It reuiqres `offset` parameter which is of `TimeMeasure` type, i.e. `100.ms`, `3.s`, etc. The `plotAt()` available for any stream. 

```kotlin
wave("dropbox:///2.wav")
    .window(1001, 501)
    .hamming()
    .fft(1024)
    .plotAt(offset = 1000.ms, freqCutOff = 0 to 4500)
```

![Plotting FFT of wave file](assets/wave_fft_plot.png "Plotting FFT of wave file")

Method has following parameters optional parameters on top:

* `sampleRate` as `Float` - the sample rate to evaluate the stream with, by default `44100.0f`.
* `freqCutOff` as `Pair<Int, Int>` - the range of the frequencies to display, by default `0 to sampleRate.toInt() / 2`.

### Data Frames

Whenever you want to plot yourself the data, you may get data in the format ready to be absorbed by `lets_plot` by calling `.dataFrame()` function:

* On `Sample` stream:
     * The parameters are:
        * `length` as `TimeMeasure` - on an infinite stream allows to trim the stream implicitly, i.e. `1.s`
        * `timeShift` as `Double` - the number of seconds to shift the stream to, if positive moves right, if negative moves left, by default `0.0`.
        * `sampleRate` as `Float` - the sample rate to evaluate the stream with, by default `44100.0f`.
     * The output table has the following columns:
        * `time` -- the time marker of the sample in seconds (Double).
        * `value` -- the double value of the sample (Double).
* On `FftSample` stream to look in motion:
    * The parameters are:
        * `length` as `TimeMeasure` - on an infinite stream allows to trim the stream implicitly, i.e. `1.s` 
        * `sampleRate` as `Float` - the sample rate to evaluate the stream with, by default `44100.0f`.
        * `freqCutOff` as `Pair<Number, Number>` - the range of the frequencies to display, by default `0 to sampleRate / 2.0`.
    * The output table has the following columns:
        * `time` -- the time marker of the sample in seconds (Double).
        * `frequency` -- the frequency in Hz (Double).
        * `value` -- the value in dB (Double).
* On `FftSample` stream to look at a specific time (the function name is `dataFrameAt()`):
    * The parameters are:
        * `offset` -- the time marker to look at of `TimeMeasure` type, i.e. `100.ms`, `3.s`, etc.
        * `sampleRate` as `Float` - the sample rate to evaluate the stream with, by default `44100.0f`.
        * `freqCutOff` as `Pair<Number, Number>` - the range of the frequencies to display, by default `0 to sampleRate / 2.0`.
    * The output table has the following columns:
        * `frequency` -- the frequency in Hz (Double).
        * `value` -- the value in dB (Double).
* On any type `T` stream:
     * The parameters are:
        * `length` as `TimeMeasure` - on an infinite stream allows to trim the stream implicitly, i.e. `1.s`
        * `timeShift` as `Double` - the number of seconds to shift the stream to, if positive moves right, if negative moves left, by default `0.0`.
        * `sampleRate` as `Float` - the sample rate to evaluate the stream with, by default `44100.0f`.
        * `mapper` as `(T) -> Any` - the mapper function to map value column from the stream values, by default the value remains intact: `mapper: (T) -> Any = { it } `
     * The output table has the following columns:
        * `time` -- the time marker of the sample in seconds (Double).
        * `value` -- the value of the sample (`T` or the type you're mapping to).

## Management server

Management server allows you to change the [runtime configuration](#runtime-configuration) parameters of the running Wave instance via REST API. To start the server specify the `MANAGEMENT_SERVER_PORT` where it is going to be accessible. Once it is started you read or write values.

* To read the value perform the `GET` request to `http://server:port/config/VARIABLE_NAME`. The `VARIABLE_NAME` can be one of the predefined ones, or your own. The value is returned as a body, if the variable is not found 404 code is returned.
* To write the value perform the `POST` request to `http://server:port/config/VARIABLE_NAME`. The `VARIABLE_NAME` can be one of the predefined ones, or your own. The value shoudl be specified in the body of request.