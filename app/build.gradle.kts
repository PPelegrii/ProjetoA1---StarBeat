import java.util.Properties

var jamendoClientId: String? = null

if (project.hasProperty("JAMENDO_CLIENT_ID")) {
    jamendoClientId = project.property("JAMENDO_CLIENT_ID").toString()
}

// 2) se não, tenta ler variável de ambiente
if (jamendoClientId == null || jamendoClientId.toString().isEmpty()) {
    val envVal = System.getenv("JAMENDO_CLIENT_ID")
    if (envVal != null && envVal.isNotEmpty()) {
        jamendoClientId = envVal
    }
}

// 3) se ainda não achou, tenta ler local.properties
if (jamendoClientId == null || jamendoClientId.toString().isEmpty()) {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        val props = Properties()
        localPropsFile.inputStream().use { stream -> props.load(stream) }
        if (props["JAMENDO_CLIENT_ID"] != null) {
            jamendoClientId = props["JAMENDO_CLIENT_ID"].toString()
        }
    }
}
// 4) literalmente se mata
if (jamendoClientId == null || jamendoClientId.toString().isEmpty()) {
    throw GradleException("JAMENDO_CLIENT_ID não encontrado. Coloque ele em local.properties or declare env var or -PJAMENDO_CLIENT_ID e POR FAVOR não de GIT PUSH NA FUCKIN API_KEY ")
}



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
}

android {
    namespace = "a1.StarBeat"
    compileSdk = 36

    defaultConfig {
        applicationId = "a1.StarBeat"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val room_version = "2.8.0"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
}
