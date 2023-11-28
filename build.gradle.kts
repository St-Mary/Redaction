plugins {
    id("java")
}

group = "me.aikoo.stmaryredactor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion", "JDA", "5.0.0-beta.12")

    implementation("io.github.cdimascio", "java-dotenv", "5.1.1")
    implementation("ch.qos.logback", "logback-classic", "1.2.9")

    // Database
    implementation("org.mariadb.jdbc:mariadb-java-client:2.1.2")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.hibernate.orm:hibernate-core:6.3.0.CR1")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.3.0.CR1")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
}

tasks.test {
    useJUnitPlatform()
}