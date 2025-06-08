plugins {
    `maven-publish`
}

dependencies {
    api(project(":shared"))
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
        }
    }
}