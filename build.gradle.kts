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
            "runtimeOnly"(libs.slf4j.simple)

            "testImplementation"(libs.junit.api)
            "testRuntimeOnly"(libs.junit.engine)
            "testRuntimeOnly"(libs.junit.launcher)
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}