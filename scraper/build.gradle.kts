plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "io.kod"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":commons"))
                implementation("io.ktor:ktor-client-core:${properties["io.ktor.version"]}")
                implementation("io.ktor:ktor-client-cio:${properties["io.ktor.version"]}")
                implementation("io.ktor:ktor-client-serialization:${properties["io.ktor.version"]}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation(project(":schema-commons"))
                // https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
                implementation("org.seleniumhq.selenium:selenium-java:3.141.59")

            }
        }
    }
}