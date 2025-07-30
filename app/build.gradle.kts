// build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.antoinehory.divination"
    // compileSdk devrait être au moins le niveau de targetSdk, et idéalement le dernier stable.
    compileSdk = 34 // Recommandé pour la stabilité avec les versions de dépendances actuelles.
    // Si vous avez besoin de fonctionnalités de SDK 35 ou 36, ajustez, mais 34 est une base solide.

    defaultConfig {
        applicationId = "fr.antoinehory.divination"
        minSdk = 29
        // targetSdk devrait être une version d'API stable et testée.
        targetSdk = 34 // Alignons sur compileSdk pour commencer.
        versionCode = 4     // Conservez vos valeurs
        versionName = "2.3" // Conservez vos valeurs

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Bon pour la production
            // Vous pouvez ajouter des optimisations R8/ProGuard plus avancées ici si nécessaire.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configuration pour la compilation Java et Kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // packagingOptions est maintenant `packaging` dans les versions récentes d'AGP
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md" // Souvent ajouté pour éviter les duplications
            excludes += "/META-INF/LICENSE-notice.md" // Idem
        }
    }
}

// Configuration spécifique à KSP (par exemple, pour Room)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    // Vous pouvez ajouter d'autres arguments pour d'autres processeurs KSP ici.
}

dependencies {
    // Jetpack Compose (utilise le BOM pour la gestion des versions)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview) // Pour les previews dans l'IDE
    implementation(libs.androidx.compose.material3)          // Composants Material 3
    implementation(libs.androidx.compose.compiler.artifact)

    // AndroidX Core et Activity
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose) // Pour l'intégration de Compose dans les Activities

    // Lifecycle (ViewModel, LiveData, etc. avec prise en charge de Compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation avec Compose
    implementation(libs.androidx.navigation.compose)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Room pour la base de données locale
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Extensions Kotlin (coroutines, Flow)
    ksp(libs.androidx.room.compiler)      // Processeur d'annotations pour Room via KSP

    // Bibliothèques UI XML (uniquement si vous avez encore des écrans/composants basés sur XML)
    // Si votre application est 100% Compose, vous pourriez envisager de les supprimer
    // pour alléger l'application, mais elles ne posent généralement pas de problème.
    implementation(libs.androidx.appcompat) // Pour la compatibilité des thèmes et des composants plus anciens
    implementation(libs.material)          // Pour com.google.android.material (vues XML)
    // implementation(libs.androidx.constraintlayout) // Décommentez si vous utilisez ConstraintLayout en XML

    // Matériel Google (pourrait être pour une version spécifique si 'libs.material' ne suffit pas)
    // Si libs.material (com.google.android.material:material) est déjà inclus et à la bonne version,
    // libs.material.v1110 pourrait être redondant. Vérifiez son utilité.
    // J'assume que vous avez une raison spécifique pour libs.material.v1110, donc je le laisse.
    // S'il s'agit de la même bibliothèque que libs.material, préférez utiliser un seul alias.
    // Votre toml a : material = { group = "com.google.android.material", name = "material", version.ref = "material" }
    // et material-v1110 = { module = "com.google.android.material:material", version.ref = "materialVersion" }
    // Si "material" et "materialVersion" dans le TOML pointent vers la même version (ex: "1.12.0"),
    // alors `implementation(libs.material)` suffit.
    // Si `materialVersion` était pour une version différente, alors `libs.material.v1110` a un sens.
    // Pour la propreté, assurons-nous qu'ils ne sont pas redondants. Si 'materialVersion' est la même que 'material',
    // alors `implementation(libs.material)` couvre déjà cela.
    // Dans notre TOML corrigé, j'ai simplifié pour n'avoir qu'un `material`.
    // Donc, la ligne `implementation(libs.material.v1110)` n'est plus nécessaire si `libs.material` est présent.

    // Tests Unitaires
    testImplementation(libs.junit)

    // Tests d'Instrumentation Android
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Pour les tests UI Compose
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Pour les tests UI Compose

    // Dépendances de débogage (outils pour Compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

