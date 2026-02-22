plugins {
    application
}

application {
    mainClass.set("com.mapatiri.speaker.App")
}

dependencies {
    implementation(project(":grammar"))
    implementation(libs.guava)
    implementation(libs.guice)
}