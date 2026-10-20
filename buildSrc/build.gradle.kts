plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.spotless.plugin)
    implementation(libs.publishdata.plugin)
    implementation(libs.maven.publish.plugin)
}
