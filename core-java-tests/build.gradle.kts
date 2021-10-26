plugins {
    id("java")
}

dependencies {
    val junitJupiterVersion: String by project

    implementation(project(":core"))

    testImplementation ("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}


tasks.withType<JavaCompile> {
    // Allows conserving parameter name of constructors
    options.compilerArgs.add("-parameters")
}
