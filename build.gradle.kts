import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.0"
    id("maven-publish")
    id("java")
}

dependencies {
    implementation(project(":eco-core"))
    implementation(project(":eco-core:core-plugin"))
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://repo.codemc.org/repository/nms/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
        maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
        maven { url = uri("https://repo.helpch.at/releases") }
    }

    dependencies {
        compileOnly("com.willfp:eco:6.77.0")
        compileOnly("org.jetbrains:annotations:23.0.0")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")

        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.0")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks {
        java {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        compileJava {
            options.encoding = "UTF-8"
            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml")) {
                expand(mapOf("projectVersion" to project.version))
            }
        }

        build {
            dependsOn("shadowJar")
        }
    }
}

group = "com.willfp"
version = findProperty("version")!!

tasks.register("buyThePlugins") {
    doLast {
        println("If you like the plugin, please consider buying it on Spigot or Polymart!")
        println("Spigot: https://www.spigotmc.org/resources/authors/auxilor.507394/")
        println("Polymart: https://polymart.org/user/auxilor.1107/")
        println("Buying gives you access to support and the plugin auto-updater, and it allows me to keep developing plugins.")
    }
}

tasks {
    build {
        dependsOn("publishToMavenLocal")
        finalizedBy("buyThePlugins")
    }

    withType<Jar> {
        destinationDirectory.set(file("$rootDir/bin/"))
    }

    register("cleanBin") {
        doLast {
            file("$rootDir/bin").deleteRecursively()
        }
    }

    clean {
        finalizedBy("cleanBin")
    }

    named<ShadowJar>("shadowJar") {
        archiveFileName.set("${findProperty("plugin-name")} v${findProperty("version")}.jar")
    }

    named<Jar>("jar") {
        archiveFileName.set("${findProperty("plugin-name")} v${findProperty("version")} unshaded.jar")
    }
}
