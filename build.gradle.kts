plugins {
    kotlin("multiplatform") version "1.9.24"
    id("org.jetbrains.compose") version "1.6.11"
}

group = "com.group"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm("desktop")
    jvmToolchain(17)

    sourceSets {
        val desktopMain by getting {
            // Your sources are in src/com/... (no src/main), so map src as sources root.
            kotlin.srcDirs("src")
            resources.srcDirs("resources", "src/resources")

            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation("org.xerial:sqlite-jdbc:3.46.0.0")
                implementation("org.slf4j:slf4j-simple:2.0.13")
            }

        }

        val desktopTest by getting {
            kotlin.srcDirs("test", "src/test")
            resources.srcDirs("test/resources", "src/test/resources")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.group.ticketmachine.desktop.DesktopMainKt"
    }
}
