plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "com.group"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    // Because your Main.kt is in package com.group.ticketmachine
    mainClass.set("com.group.ticketmachine.MainKt")
}

sourceSets {
    main {
        kotlin.srcDirs("src")
        resources.srcDirs("resources")
    }
    test {
        kotlin.srcDirs("test")
    }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
