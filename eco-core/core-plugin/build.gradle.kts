group = "com.willfp"
version = rootProject.version

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    compileOnly(fileTree("../../lib") { include("*.jar") })
}

tasks {
    build {
        dependsOn("publishToMavenLocal")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
