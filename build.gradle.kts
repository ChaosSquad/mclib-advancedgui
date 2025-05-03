plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.8"
    id("maven-publish")
}

group = "net.chaossquad"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "respark-releases"
        url = uri("https://maven.respark.dev/releases")
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.leoko.advancedgui:AdvancedGUI:2.2.9")
}

java {
    withSourcesJar()
    withJavadocJar()
}

// gradle publish{PUBLICATION_NAME}To{REPOSITORY_NAME}Repository
// in this case: publishMavenToChaosSquadRepository
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "chaossquad"
            url = uri(if (version.toString().endsWith("RELEASE")) {
                "https://maven.chaossquad.net/releases"
            } else {
                "https://maven.chaossquad.net/snapshots"
            })

            credentials {
                username = findProperty("chaossquad-repository.username") as String?
                password = findProperty("chaossquad-repository.password") as String?
            }
        }
    }
}
