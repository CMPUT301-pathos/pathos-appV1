plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventlottery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pathos.eventlottery"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configurations.configureEach {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Circular profile images
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // QR / barcode
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:20.0.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    implementation(libs.navigation.fragment)

    implementation(libs.espresso.idling.resource)
    implementation(libs.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation(libs.rules)

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.12.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}