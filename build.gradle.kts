import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion: String by System.getProperties()

    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion

    `java-library`
    `maven-publish`
    signing
}

group = "io.wavebeans.jupyter"
version = properties["version"].toString().let {
    if (it.endsWith("-SNAPSHOT"))
        it.removeSuffix("-SNAPSHOT") + "." + System.currentTimeMillis().toString()
    else
        it
}

val spekVersion: String by System.getProperties()
val kotlinxSerializationRuntimeVersion: String by System.getProperties()
val wavebeansVersion: String by System.getProperties()
val letsPlotVersion: String by System.getProperties()
val letsPlotKotlinApiKernelVersion: String by System.getProperties()
val javalinVersion: String by System.getProperties()

apply {
    plugin("kotlin")
}

repositories {
    jcenter()
    maven("https://jetbrains.bintray.com/lets-plot-maven")
//    maven("https://dl.bintray.com/kotlin/kotlin-eap")
//    maven("https://kotlin.bintray.com/kotlinx")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    api("io.wavebeans:lib:$wavebeansVersion")
    api("io.wavebeans:exe:$wavebeansVersion")
    api("io.wavebeans:http:$wavebeansVersion")
    api("io.wavebeans:filesystems-core:$wavebeansVersion")
    api("io.wavebeans:filesystems-dropbox:$wavebeansVersion")
    api("io.wavebeans:metrics-core:$wavebeansVersion")
    api("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
    api("org.jetbrains.lets-plot-kotlin:lets-plot-kotlin-api:$letsPlotKotlinApiKernelVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationRuntimeVersion")
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("io.github.microutils:kotlin-logging:1.7.7")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.13")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("com.konghq:unirest-java:3.7.04")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    systemProperty("spek2.execution.test.timeout", 0)
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

publishing {
    publications {
        create<MavenPublication>("jupyter-wave") {
            groupId = "io.wavebeans"
            artifactId = "jupyter-wave"
            version = project.version.toString()

            from(components["java"])

            populatePom(
                    "WaveBeans Jupyter plugin",
                    "Seamless integration of WaveBeans into Jupyter notebooks on top of Kotlin-Jupyter plugin"
            )
        }
    }
    repositories {
        maven {
            name = "WaveBeansMavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("maven.user")?.toString() ?: ""
                password = findProperty("maven.key")?.toString() ?: ""
            }
        }
    }
}

if (!project.hasProperty("skip.signing")) {
    signing {
        sign(publishing.publications)
    }
}

fun MavenPublication.populatePom(
        nameValue: String,
        descriptionValue: String
) {
    pom {
        name.set(nameValue)
        description.set(descriptionValue)
        url.set("https://wavebeans.io")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        scm {
            url.set("https://github.com/WaveBeans/wavebeans")
            connection.set("scm:git:git://github.com:WaveBeans/wavebeans.git")
            developerConnection.set("scm:git:ssh://github.com:WaveBeans/wavebeans.git")
        }
        developers {
            developer {
                name.set("Alexey Subbotin")
                url.set("https://github.com/asubb")
            }
        }
    }
}
