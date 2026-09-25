plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.qa.tictactoe"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.qa.tictactoe"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Lets tests save files that the Gradle run pulls from the device (see FailureArtifacts).
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"
    }

    // Test data sits next to the code that reads it; this packs the data files into the test APK.
    sourceSets["androidTest"].resources.directories += "src/androidTest/java/testdata"

    testOptions {
        // Without it the app's launch transition moves the window while calibration reads cell bounds.
        animationsDisabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Tests run in the host app's process, which loads native libraries only from the host APK.
    implementation(libs.opencv)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.test.services.storage)
    androidTestUtil(libs.androidx.test.services)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.snakeyaml)
    androidTestImplementation(libs.kotlinx.serialization.json)
}

// Every test targets a known bug, so CI passes -PignoreTestFailures: the build then fails only on setup problems,
// and the test results are published as a separate check.
if (providers.gradleProperty("ignoreTestFailures").isPresent) {
    tasks.named { it == "connectedDebugAndroidTest" }.configureEach { (this as VerificationTask).ignoreFailures = true }
}

// The app under test is a prebuilt APK: -Paut=<path to it> installs it before the tests run.
providers.gradleProperty("aut").orNull?.let { aut ->
    val installAut = tasks.register<Exec>("installAut") {
        executable(androidComponents.sdkComponents.adb.get().asFile)
        args("install", "-r", rootProject.file(aut))
    }
    tasks.named { it == "connectedDebugAndroidTest" }.configureEach { dependsOn(installAut) }
}
