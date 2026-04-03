plugins {
    application
}

application {
    mainClass.set("com.mapatiri.speaker.App")
}

dependencies {
    implementation(project(":grammar"))
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.guice)

    implementation(libs.logback.core)
    implementation(libs.logback.classic)
}