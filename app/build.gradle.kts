import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.3"
    application
}

application {
    mainClass.set("yrkesskade.AppKt")
}

val aapLibsVersion = "3.7.54"
val ktorVersion = "2.3.3"

dependencies {
    //implementation("com.github.navikt.aap-vedtak:kafka-dto:1.1.32")

    implementation("com.github.navikt.aap-libs:ktor-auth-azuread:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:kafka-2:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:ktor-utils:$aapLibsVersion")
    implementation("org.apache.kafka:kafka-clients:3.5.1")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.11.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.9")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.4")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test-2:$aapLibsVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
//    maven("https://packages.confluent.io/maven/") // transitive avro dependency
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "19"
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")