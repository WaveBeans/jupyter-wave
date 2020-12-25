import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion: String by System.getProperties()

    kotlin("jvm") version kotlinVersion
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion

    `java-library`
    `maven-publish`
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
    mavenCentral()
    mavenLocal()
    repositories {
        maven {
            name = "Bintray WaveBeans"
            url = uri("https://dl.bintray.com/wavebeans/wavebeans")
        }
    }
    maven("https://jetbrains.bintray.com/lets-plot-maven")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlinx")
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
    implementation("io.github.microutils:kotlin-logging:1.7.7")
    implementation("io.wavebeans:lib:$wavebeansVersion")
    implementation("io.wavebeans:exe:$wavebeansVersion")
    implementation("io.wavebeans:http:$wavebeansVersion")
    implementation("io.wavebeans.filesystems:dropbox:$wavebeansVersion")
    implementation("io.wavebeans.metrics:core:$wavebeansVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationRuntimeVersion")
    implementation("org.jetbrains.lets-plot-kotlin:lets-plot-kotlin-api:$letsPlotKotlinApiKernelVersion")
    implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
    implementation("io.javalin:javalin:$javalinVersion")
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
            groupId = "io.wavebeans.jupyter"
            artifactId = "wave"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

bintray {
    user = findProperty("bintray.user")?.toString() ?: ""
    key = findProperty("bintray.key")?.toString() ?: ""
    setPublications("jupyter-wave")
    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = "wavebeans"
        name = "wavebeans"
        userOrg = "wavebeans"
        vcsUrl = "https://github.com/WaveBeans/wavebeans"
        setLicenses("Apache-2.0")
        publish = true
        version(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.VersionConfig> {
            name = project.version.toString()
        })
    })
}
