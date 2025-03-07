import com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.4"
	id("io.spring.dependency-management") version "1.1.0"

	kotlin("jvm") version "1.8.10"
	kotlin("plugin.spring") version "1.8.10"

	id("com.netflix.dgs.codegen") version "5.7.0" //https://plugins.gradle.org/plugin/com.netflix.dgs.codegen
}

// extra["graphql-java.version"] = "19.2"

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:6.0.01"))
	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter") {
		exclude("org.yaml", "snakeyaml")
	}
	implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets-autoconfigure") {
		exclude("org.yaml", "snakeyaml")
	}
	implementation("org.yaml:snakeyaml:2.0")

	//Spring and kotlin
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	// test
	testImplementation( "com.netflix.graphql.dgs:graphql-dgs-client")
	testImplementation("org.springframework:spring-webflux")
	testImplementation("io.projectreactor.netty:reactor-netty")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<GenerateJavaTask> {
	schemaPaths =
		mutableListOf("${projectDir}/src/main/resources/schema") // List of directories containing schema files
	packageName = "com.example.demo.gql" // The package name to use to generate sources
	generateClient = true // Enable generating the type safe query API
	shortProjectionNames = false
	maxProjectionDepth = 2
	snakeCaseConstantNames = true
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
