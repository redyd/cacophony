plugins {
    application
}

application {
    mainClass.set("com.mapatiri.node.App")
}

dependencies {
    implementation(project(":grammar"))
    implementation(libs.guava)
    implementation(libs.guice)
}