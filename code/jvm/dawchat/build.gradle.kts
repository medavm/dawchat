plugins {
    kotlin("jvm") version "2.0.20" apply true
    kotlin("plugin.spring") version "1.9.25" apply true
    id("org.springframework.boot") version "3.3.3"  apply true
    id("io.spring.dependency-management") version "1.1.6"  apply true

}

group = "pt.isel.daw.chatapp.g12"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // for JDBI and Postgres
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // To get password encode
    implementation("org.springframework.security:spring-security-core:6.3.2")

    // To use WebTestClient on tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")


    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.test {
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        environment("DB_URL", "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
    }

    dependsOn(":dbTestsWait")
    finalizedBy(":dbTestsDown")
}

kotlin {
    jvmToolchain(21)
}


/**
 * DB related tasks
 * - To run `psql` inside the container, do
 *      docker exec -ti db-tests psql -d db -U dbuser -W
 *   and provide it with the same password as define on `tests/Dockerfile-db-test`
 */

//val composeFileDir: Directory by parent!!.extra
//val dockerComposePath = composeFileDir.file("docker-compose.yml").toString()
val dockerComposePath = "test/docker/docker-compose.yml"
val dockerFilePath = "test/docker/tests/Dockerfile-db-test"

task<Exec>("dbTestsUp") {
    commandLine("docker", "compose", "-f", dockerComposePath, "up", "-d", "--build", "--force-recreate", "db-tests")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "db-tests", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbTestsUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker", "compose", "-f", dockerComposePath, "down", "db-tests")
}

task<Exec>("dbDemoCreate"){
    //commandLine("docker", "build", "-f", dockerFilePath, "-t", "demo-db-img", ".")

    commandLine("docker", "run", "-d",
        "-e", "POSTGRES_USER=dbuser", "-e", "POSTGRES_PASSWORD=changeit", "-e", "POSTGRES_DB=db",
        "-p", "5432:5432", "--name", "demo-db" , "demo-db-img")
}

task<Exec>("dbDemoUp"){
    commandLine("docker", "start", "demo-db")
}

task<Exec>("dbDemoDown") {
    commandLine("docker", "stop", "demo-db")
}

task<Exec>("dbDemoClean") {
    commandLine("docker", "container", "rm", "demo-db")

    dependsOn("dbDemoDown")
}


