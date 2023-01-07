
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.21"
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.laughnman"
version = "0.3.1"

val koinVersion = "3.3.2"
val kotestVersion = "5.5.4"
val ktorVersion = "2.2.1"
val awsVersion = "2.19.8"

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")

	implementation("io.insert-koin:koin-core:$koinVersion")
	implementation("info.picocli:picocli:4.7.0")

	implementation("io.ktor:ktor-client-cio:$ktorVersion")
	implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

	implementation("software.amazon.awssdk:s3:$awsVersion")
	implementation("software.amazon.awssdk:netty-nio-client:$awsVersion")

	implementation("ch.qos.logback:logback-classic:1.4.5")
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

	testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
	testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
	testImplementation("io.mockk:mockk:1.13.2")
}

val compileKotlin: KotlinCompile by tasks
// compileKotlin.kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"

/*
java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}
*/

tasks {
	named<ShadowJar>("shadowJar") {
		archiveClassifier.set("")
		archiveVersion.set("")
		mergeServiceFiles()
		manifest {
			attributes(mapOf(
				"Main-Class" to "org.laughnman.multitransfer.ApplicationKt",
				"Version" to project.version
			))
		}
	}
}

tasks {
	build {
		dependsOn(shadowJar)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}