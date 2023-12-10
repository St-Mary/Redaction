plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "com.stmarygate.redaction"
version = "1.0.0"

repositories {
    mavenCentral()
}

project.setProperty("mainClassName", "com.stmarygate.redaction.Main")

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion", "JDA", "5.0.0-beta.12")

    implementation("io.github.cdimascio", "java-dotenv", "5.1.1")
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("ch.qos.logback", "logback-classic", "1.2.9")

    // Database
    implementation("org.postgresql:postgresql:42.7.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.hibernate.orm:hibernate-core:6.3.0.CR1")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.3.0.CR1")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
}

tasks {
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.stmarygate.redaction.Main"
    }

    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("stmary-redaction")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.test {
    useJUnitPlatform()
}