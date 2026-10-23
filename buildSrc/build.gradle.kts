plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.spotless)
    implementation(libs.gradle.publishdata)
    implementation(libs.gradle.vanniktech)
}
