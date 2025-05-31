plugins {
    application
    alias(libs.plugins.jooq.codegen)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.bundles.jooq)
    implementation(libs.sqlite.jdbc)
    implementation(libs.bundles.log4j)
    implementation(libs.minimessage)
    implementation(libs.clikt)

    jooqCodegen(libs.jooq.meta.extensions)
}

application {
    mainClass.set("club.mcsports.droplet.friends.launcher.LauncherKt")
}

sourceSets {
    main {
        java {
            srcDirs(
                "build/generated/source/db/main/java",
            )
        }
        resources {
            srcDirs(
                "src/main/db"
            )
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn(tasks.jooqCodegen)
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            target {
                directory = "build/generated/source/db/main/java"
                packageName = "club.mcsports.droplet.friends.generated.db"
            }
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    // Specify the location of your SQL script.
                    // You may use ant-style file matching, e.g. /path/**/to/*.sql
                    //
                    // Where:
                    // - ** matches any directory subtree
                    // - * matches any number of characters in a directory / file name
                    // - ? matches a single character in a directory / file name
                    property {
                        key = "scripts"
                        value = "src/main/db/schema.sql"
                    }

                    // The sort order of the scripts within a directory, where:
                    //
                    // - semantic: sorts versions, e.g. v-3.10.0 is after v-3.9.0 (default)
                    // - alphanumeric: sorts strings, e.g. v-3.10.0 is before v-3.9.0
                    // - flyway: sorts files the same way as flyway does
                    // - none: doesn't sort directory contents after fetching them from the directory
                    property {
                        key = "sort"
                        value = "semantic"
                    }

                    // The default schema for unqualified objects:
                    //
                    // - public: all unqualified objects are located in the PUBLIC (upper case) schema
                    // - none: all unqualified objects are located in the default schema (default)
                    //
                    // This configuration can be overridden with the schema mapping feature
                    property {
                        key = "unqualifiedSchema"
                        value = "none"
                    }

                    // The default name case for unquoted objects:
                    //
                    // - as_is: unquoted object names are kept unquoted
                    // - upper: unquoted object names are turned into upper case (most databases)
                    // - lower: unquoted object names are turned into lower case (e.g. PostgreSQL)
                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                }
            }
        }
    }
}