plugins {
    kotlin("multiplatform") version "1.9.0"
}

group = "com.ibis"
version = "1.2"

repositories {
    mavenCentral()
}

kotlin {
    mingwX64("native") {
        binaries {
            executable()
        }
    }
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.4.0")
            }
        }
    }
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.BIN
}
