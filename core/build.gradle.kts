import java.net.URI

plugins {
    id("org.jlleitschuh.gradle.ktlint")

    id("maven-publish")
    id("signing")
}

dependencies {
    val kotestVersion: String by project
    val mockkVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    api("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "csv-core"
            from(components.findByName("java"))

            pom {
                name.set("Kotlin stream CSV core")
                description.set(
                    "A kotlin and java CSV parser that uses the power of kotlin DSL to simplify parsing and error handling compared to existing solutions"
                )
                url.set("https://github.com/pelletier197/kotlin-stream-csv")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("pelletier197")
                        name.set("Sunny Pelletier")
                        email.set("sunnypelletier01@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/pelletier197/kotlin-stream-csv.git")
                    developerConnection.set("scm:git:git@github.com:pelletier197/kotlin-stream-csv.git")
                    url.set("https://github.com/pelletier197/")
                }
            }
        }
    }

    repositories {
        maven {
            url = URI.create("https://oss.sonatype.org/service/local/staging/deploy/maven2")

            credentials {
                username = System.getenv("SONATYPE_NEXUS_USERNAME")
                password = System.getenv("SONATYPE_NEXUS_PASSWORD")
            }
        }
    }
}

configure<SigningExtension> {
    val signingKey = System.getProperty("signingKey")
    val signingPassword = System.getProperty("signingPassword")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign("mavenJava")
}
//
//configure<NexusStagingExtension> {
//    username = System.getenv("SONATYPE_NEXUS_USERNAME")
//    password = System.getenv("SONATYPE_NEXUS_PASSWORD")
//}
