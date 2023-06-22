
plugins {
  `kotlin-dsl`
}

repositories {
  google()
  gradlePluginPortal()
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
  implementation("com.android.tools.build:gradle:7.4.2")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
  implementation("com.github.gmazzo:gradle-buildconfig-plugin:3.1.0")

  implementation(gradleApi())
  implementation(localGroovy())
}