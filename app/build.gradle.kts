plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}



android {
    namespace = "com.example.gps_locatorcw"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gps_locatorcw"
        minSdk = 26
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }


}



dependencies {

    implementation("androidx.room:room-runtime:2.6.1") // Update version
    implementation("androidx.room:room-common:2.6.1") // Update version
    annotationProcessor("androidx.room:room-compiler:2.6.1") // Update version


    implementation("androidx.appcompat:appcompat:1.4.0") // Updated version
    implementation("com.google.android.material:material:1.5.0") // Updated version
    implementation("androidx.constraintlayout:constraintlayout:2.1.3") // Updated version
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.google.android.gms:play-services-maps:17.0.1")
    implementation ("androidx.work:work-runtime:2.7.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3") // Updated version
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0") // Updated version
    constraints{
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0"){
            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")

        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0"){
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib");
        }
    }
}
