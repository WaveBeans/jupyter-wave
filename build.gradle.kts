import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion: String by System.getProperties()

    kotlin("jvm") version kotlinVersion

    `java-library`
    `maven-publish`
}

allprojects {

    group = "io.wavebeans.jupyter"

    val spekVersion: String by System.getProperties()
    val kotlinxSerializationRuntimeVersion: String by System.getProperties()

    apply {
        plugin("kotlin")
    }

    repositories {
        jcenter()
        mavenCentral()
        mavenLocal()
        maven ("https://dl.bintray.com/kotlin/kotlin-eap")
        maven ("https://kotlin.bintray.com/kotlinx")
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
        implementation("io.wavebeans:lib:0.1.0-SNAPSHOT")
        implementation("io.wavebeans:exe:0.1.0-SNAPSHOT")
        implementation("io.wavebeans:http:0.1.0-SNAPSHOT")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationRuntimeVersion")

        testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
        testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
        testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.13")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
        testImplementation("ch.qos.logback:logback-classic:1.2.3")

    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.test {
        systemProperty("SPEK_TIMEOUT", 0)
        useJUnitPlatform {
            includeEngines("spek2")
        }
        maxHeapSize = "2g"
    }

    tasks.jar {
        manifest {
            attributes(
                    "WaveBeans-Version" to properties["version"]
            )
        }
    }
}

publishing {
  publications {
    create<MavenPublication>("jupyter-wave") {
        groupId = "io.wavebeans.jupyter"
        artifactId = "wave"

        from(components["java"])
    }    
  }
}