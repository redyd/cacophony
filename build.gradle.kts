allprojects {
    group = "com.mapatiri"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.apply("java-library")

    afterEvaluate {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        dependencies {
            "implementation"(libs.slf4j.api)

            "testImplementation"(libs.junit.api)
            "testImplementation"(libs.mockito)
            "testImplementation"(libs.mockito.jupiter)

            "testRuntimeOnly"(libs.junit.engine)
            "testRuntimeOnly"(libs.junit.launcher)
            "testRuntimeOnly"(libs.logback.classic)
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}