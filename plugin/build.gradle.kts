plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":api-lite"))
    implementation(libs.bundles.cloud.command)
    compileOnly(libs.velocity)
    kapt(libs.velocity)
}

tasks.shadowJar {
    exclude("io.netty")
    exclude("app.simplecloud")
    relocate("io.grpc", "club.mcsports.droplet.friends.relocate.io.grpc")
    relocate("com.google.protobuf", "club.mcsports.droplet.friends.relocate.google.protobuf")
    relocate("com.google.common", "club.mcsports.droplet.friends.relocate.google.common")
    mergeServiceFiles()
    archiveFileName.set("${rootProject.name}-${project.name}.jar")
}