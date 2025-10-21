import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.coooldoggy.deeplink"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.coooldoggy.deeplink.sdk"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            
            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "Deferred Deep Link SDK"
        description = "Kotlin Multiplatform SDK for Deferred Deep Linking (Android & iOS)"
        inceptionYear = "2025"
        url = "https://github.com/coooldoggy/deferred-deeplink-system/"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "coooldoggy"
                name = "coooldoggy"
                url = "https://github.com/coooldoggy"
            }
        }
        scm {
            url = "https://github.com/coooldoggy/deferred-deeplink-system"
            connection = "scm:git:git://github.com/coooldoggy/deferred-deeplink-system.git"
            developerConnection = "scm:git:ssh://github.com/coooldoggy/deferred-deeplink-system.git"
        }
    }
}
