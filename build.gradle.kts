group = "com.github.aryanbarthwal"
version = "1.2.0"

plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidxRoom) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

allprojects {
    group = rootProject.group
}
