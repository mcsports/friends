plugins {
    `maven-publish`
}

dependencies {
    api(project(":shared"))
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveFileName.set("${rootProject.name}-${project.name}.jar")
}

publishing {
    repositories {
        maven {
            name = "MCSports"
            url = uri("https://repo.mcsports.club/releases")
            credentials {
                username = "deploy"
                password = System.getenv("REPO_TOKEN") ?: ""
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            val hash = System.getenv("COMMIT_HASH")
            print("Hash: $hash")
            version = if (hash != null) "${rootProject.version}-$hash" else rootProject.version.toString()
        }
    }
}