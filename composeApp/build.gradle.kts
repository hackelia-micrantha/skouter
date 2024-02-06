import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.nativeCocoapods)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.apolloGraphQL)
    alias(libs.plugins.kotlinSerialization)

    id("com.micrantha.bluebell")
}

kotlin {
    cocoapods {
        version = "1.0"
        summary = "Native dependencies for ${project.name}"

        framework {
            baseName = "bluebell"
        }

        pod("Reachability")
        //pod("MediaPipeTasksVision")
        //podfile = project.file("../iosApp/Podfile")
    }

    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "composeApp"
            isStatic = true
            binaryOption("bundleId", "com.micrantha.skouter")
        }
        iosTarget.compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xallocator=custom")
            }
        }
    }

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)

            implementation(libs.kodein.di)
            implementation(libs.kodein.di.framework.compose)
            implementation(libs.kodein.di.conf)

            implementation(libs.okio)

            implementation(libs.cache4k)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.kodein)

            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            implementation(libs.supabase.gotrue)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.apollo.graphql)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.realtime)

            implementation(libs.permissions.compose)
            implementation(libs.geo.compose)
            implementation(libs.kamel.image)
            implementation(libs.moko.media)

            //implementation("ca.rmen:rhymer:1.2.0")

            //implementation("org.hashids:hashids:1.0.3")

        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)

            implementation(libs.androidx.lifecycle.viewmodel.ktx)

            implementation(libs.ktor.client.android)
            implementation(libs.androidx.lifecycle.runtime.ktx)

            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.fragment.ktx)

            implementation(libs.androidx.palette.ktx)

            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.video)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.extensions)

            implementation(libs.tensorflow.lite)
            implementation(libs.tensorflow.lite.gpu)
            implementation(libs.tensorflow.lite.support)
            implementation(libs.tensorflow.lite.metadata)
            implementation(libs.tensorflow.lite.task.vision)
            implementation(libs.tensorflow.lite.gpu.delegate.plugin)

            implementation(libs.mediapipe.tasks.vision)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.micrantha.skouter"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

apollo {
    service("skouter") {
        packageNamesFromFilePaths("com.micrantha.skouter.graphql")
    }
}

bluebell {
    config {
        packageName = "com.micrantha.skouter"
        className = "SkouterConfig"
        fileName = "skouter.properties"
    }
    models {
        files = mapOf(
            "classification/image/efficientnet_lite0.tflite" to "classification/image.tflite",
            "detection/image/efficientdet_lite0.tflite" to "detection/image.tflite",
            "embedding/image/mobilenet_v3_small.tflite" to "embedding/image.tflite",
            "segmentation/deeplab_v3.tflite" to "segmentation/image.tflite"
        )
    }
}
