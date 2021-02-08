import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

group = "github.bb441db.ktor"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("reflect"))

    kapt("com.google.auto.service:auto-service:1.0-rc4")

    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":annotations"))
    implementation("io.ktor:ktor-client-core:1.5.1")
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.squareup:kotlinpoet-metadata:1.7.2")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.7.2")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.7.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.5")
}

tasks.test {
    useJUnitPlatform()
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}