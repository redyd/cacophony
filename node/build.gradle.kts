plugins {
    application
}

application {
    mainClass.set("com.mapatiri.node.App")
}

dependencies {
    implementation(project(":grammar"))
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.guice)

    implementation(libs.logback.core)
    implementation(libs.logback.classic)
}