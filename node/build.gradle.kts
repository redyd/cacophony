plugins {
    application
}

application {
    mainClass.set("com.mapatiri.node.App")
}

dependencies {
    implementation(project(":grammar"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.guava)
    implementation(libs.guice)
}