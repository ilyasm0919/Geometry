
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.compose") version "1.2.1"
}

group = "com.ibis"
version = "2.2.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.2.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "Geometry Desktop"
            packageVersion = version as String
            description = "Geometry editor for Desktop"
            vendor = "IBIS"

            windows {
                iconFile.set(project.file("icon.ico"))
                menuGroup = "Maths"
                upgradeUuid = "AC3FFA94-52F8-4763-8317-3157A3CADBD"
            }
        }
    }
}