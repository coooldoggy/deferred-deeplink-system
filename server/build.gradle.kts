plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
}

group = "com.coooldoggy.deeplink"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Oracle Database
    runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
    
    // H2 Database (for local testing without DB)
    runtimeOnly("com.h2database:h2:2.2.224")
    
    // Redis (for caching device fingerprints) - optional
    implementation("org.springframework.boot:spring-boot-starter-data-redis") {
        isTransitive = false  // Redis 없어도 시작 가능
    }
    compileOnly("redis.clients:jedis:5.1.0")
    
    // UA Parser (for User-Agent parsing)
    implementation("com.github.ua-parser:uap-java:1.5.4")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

