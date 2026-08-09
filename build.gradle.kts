import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.willfp"
version = findProperty("version")!!
val ecoVersion = findProperty("eco-version")

base {
    archivesName.set(project.name)
}

dependencies {
    project.project(project(":eco-core").path).subprojects {
        implementation(this)
    }
}

publishing {
    publications {
        // maven-private: only the shaded jar
        create<MavenPublication>("private") {
            artifactId = rootProject.name
        }
        // maven-releases (served publicly via the maven-public group): the API jar
        create<MavenPublication>("release") {
            artifactId = rootProject.name
        }
    }
    repositories {
        maven {
            name = "Auxilor"
            url = uri("https://repo.auxilor.io/repository/maven-private/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "AuxilorReleases"
            url = uri("https://repo.auxilor.io/repository/maven-releases/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

// Neither publication is attached to a software component, so only the single jar
// and its pom are published - no sources, javadoc, or classified variants.
afterEvaluate {
    // shadowJar defaults to the "all" classifier, which would leave maven-private
    // without a main artifact.
    publishing.publications.named<MavenPublication>("private") {
        artifact(tasks.named("shadowJar")) {
            classifier = ""
        }
    }
    // The public artifact is what other plugins compile against, so it must be the
    // plain jar, not shadowJar: shadowJar drops META-INF (taking the .kotlin_module
    // with it, which hides every top-level declaration from the Kotlin compiler) and
    // relocates kotlin.* into com.willfp.eco.libs.kotlin, which rewrites @kotlin.Metadata
    // and makes the whole API read as Java. eco publishes its API the same way.
    publishing.publications.named<MavenPublication>("release") {
        artifact(project(":eco-core:core-plugin").tasks.named<Jar>("jar")) {
            classifier = ""
        }
    }
}

tasks.matching { it.name.startsWith("generatePomFileFor") }.configureEach {
    mustRunAfter(tasks.named("clean"))
}
tasks.register("publishToAuxilor") {
    dependsOn(
        "publishPrivatePublicationToAuxilorRepository",
        "publishReleasePublicationToAuxilorReleasesRepository",
    )
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.helpch.at/releases")
    }

    dependencies {
        compileOnly("com.willfp:eco:$ecoVersion")
        compileOnly("org.jetbrains:annotations:26.0.2")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")

        compileOnly("me.clip:placeholderapi:2.11.7")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks {
        shadowJar {
            exclude("META-INF/**")
            relocate("kotlin", "com.willfp.eco.libs.kotlin")
            relocate("kotlin.jvm", "com.willfp.eco.libs.kotlin.jvm")
            relocate("kotlin.coroutines", "com.willfp.eco.libs.kotlin.coroutines")
            relocate("kotlin.reflect", "com.willfp.eco.libs.kotlin.reflect")
        }

        compileKotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml")) {
                expand(
                    "version" to project.version,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

tasks {
    build {
        dependsOn("uberJar")
    }

    register("uberJar", Jar::class.java) {
        destinationDirectory.set(file("$rootDir/bin"))
        archiveFileName.set("${project.name} v${project.version}.jar")

        dependsOn(getByName("shadowJar"))

        from(
            getByName("shadowJar").outputs.files.map { file -> zipTree(file) }

        )
    }

    register("cleanJar") {
        doLast {
            file("$rootDir/bin").deleteRecursively()
        }
    }

    clean {
        dependsOn(getByName("cleanJar"))
    }
}