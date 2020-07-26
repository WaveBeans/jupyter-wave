# Jupyter Wave

Jupyter + WaveBeans plugins and integration

Based on:

* [kotlin-jupyter](https://github.com/Kotlin/kotlin-jupyter)
* Kotlin 1.4 version of [WaveBeans](https://github.com/WaveBeans/wavebeans/pull/72)

Project status: underlying projects are in early alpha stage, moreover project bases on non-stable version of Kotlin and experimental kotlin Scripting API, hence the extension should also be considered experimental as well.

## Jupyter Plugin

The Kotlin Jupyter plugin supports adding libraries. This is how Jupyter-Wave runs, it is done via adding file `jupyter/jupyter-wave.json`. The file itself is a template which expects the library version and WaveBeans version specified during build time. The versions are specified in `gradle.properties` file.

Overall follow [kotlin-jupyter](https://github.com/Kotlin/kotlin-jupyter) documentation how to add the library.

## Running via Docker

The recommended way to run Jupyter instance is via Docker image. In `jupyter/` directory you can find `Dockerfile` which build the image. There is a `build.sh` script that automates the build and run (via `andRun` parameter) of the docker image.

## Runtime Configuration

The evaluator can be parameterized via environment variables:

1. Pre-populate DropBox configuration. DropBox File Driver can be configured automatically if `DROPBOX_CLIENT_IDENTIFIER` and `DROPBOX_ACCESS_TOKEN` is specified  