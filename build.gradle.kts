

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")  // La versión de Gradle que tengas
        classpath("com.google.gms:google-services:4.3.15") // El classpath para Firebase
    }
}