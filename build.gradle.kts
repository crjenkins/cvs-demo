// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kspPlugin) apply false
}

buildscript {
    dependencies {
        classpath(libs.gradlePlugin.android)
                classpath(libs.gradlePlugin.kotlin)
                classpath(libs.gradlePlugin.hilt)
//        classpath "com.google.dagger:hilt-android-gradle-plugin:2.44"
    }
}