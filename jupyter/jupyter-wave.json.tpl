{
  "properties": {
    "apiVersion": "$VERSION",
    "libraryVersion": "$WAVEBEANS_VERSION"
  },
  "link": "https://github.com/WaveBeans/jupyter-wave",
  "repositories": [
    "file:///home/jovyan/maven-local/repository"
  ],
  "dependencies": [
    "io.wavebeans.jupyter:wave:$apiVersion",
    "io.wavebeans:lib:$libraryVersion",
    "io.wavebeans:exe:$libraryVersion",
    "io.wavebeans:http:$libraryVersion",
    "io.wavebeans.filesystems:core:$libraryVersion",
    "io.wavebeans.filesystems:dropbox:$libraryVersion",
    "io.wavebeans.metrics:core:$libraryVersion",
    "io.ktor:ktor-server-core:1.3.2",
    "io.ktor:ktor-server-netty:1.3.2",
    "io.ktor:ktor-serialization:1.3.2",
    "io.github.microutils:kotlin-logging:1.7.7",
    "ch.qos.logback:logback-classic:1.2.3",
    "com.dropbox.core:dropbox-core-sdk:3.1.4"
  ],
  "imports": [
    "io.wavebeans.jupyter.*",
    "io.wavebeans.lib.*",
    "io.wavebeans.lib.io.*",
    "io.wavebeans.lib.math.*",
    "io.wavebeans.lib.stream.*",
    "io.wavebeans.lib.stream.fft.*",
    "io.wavebeans.lib.stream.window.*",
    "io.wavebeans.lib.table.*",
    "io.wavebeans.fs.dropbox.*",
    "java.util.concurrent.TimeUnit.*"
  ],
  "init": [
    "Evaluator.initEnvironment()",
    "// Load library JS",
    "DISPLAY(HTML(Evaluator.getInitJsHtml()))"
  ],
  "renderers": {
    "io.wavebeans.jupyter.PreviewSampleBeanStream": "HTML($it.renderPreview())"
  }
}