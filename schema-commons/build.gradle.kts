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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
            }
        }
        val commonTest by getting {
            dependencies {
/*                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))*/
            }
        }
    }
}