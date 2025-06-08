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
            version = "${rootProject.version}"
            System.getenv("COMMIT_HASH")?.let { hash ->
                version = "${rootProject.version}-$hash"
            }
        }
    }
}