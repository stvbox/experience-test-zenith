import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.ewerk.gradle.plugins.querydsl") version "1.0.10"
    id("org.liquibase.gradle") version "2.1.1"

    id("com.palantir.docker") version "0.33.0"
    id("com.palantir.docker-run") version "0.33.0"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
    kotlin("kapt") version "1.7.0"
}

group = "test.crud"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("com.h2database:h2")
    liquibaseRuntime("info.picocli:picocli:4.6.3")
    liquibaseRuntime("org.yaml:snakeyaml:1.30")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.data:spring-data-rest-hal-explorer")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.9")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.9")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.querydsl:querydsl-jpa")
    implementation("org.liquibase:liquibase-core")

    //compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.h2database:h2")

    annotationProcessor("org.projectlombok:lombok")
    kapt(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa")

    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.apache.httpcomponents:httpclient:4.5.13")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

liquibase {
    activities {
        activities.register("main") {
            this.arguments = mapOf(
                "logLevel" to "info",
                "changeLogFile" to "src/main/resources/db/changelog/db.changelog-generate.yaml",
                "url" to "jdbc:h2:file:./db/h2.data;",
                "driver" to "org.h2.Driver"
            )
        }
        runList = "main"
    }
}

val archiveBaseName = tasks.bootJar.get().archiveBaseName.get()
var dockerName = "${project.group}.$archiveBaseName"

// Глючное...
docker {
    name = dockerName
    setDockerfile(File("./Dockerfile"))
    // залепа - несчитается контрольная сумма jar файла
    copySpec.from(tasks.bootJar.get().outputs.files.singleFile).into("/build/libs")
}

// Вот плагины я ещё не отлаживал...
// чего они не берут параметры из docker да еще и падают без вести
dockerRun {
    name = dockerName
    image = dockerName
    network = "bridge"
    ports("8080:8080")
    volumes(mapOf("db" to "/ext"))
    env(mapOf("ZENITH_APP_FOLDER" to "/ext"))
}
