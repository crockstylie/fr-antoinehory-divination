pluginManagement {
    repositories {
        google() // Simplifié, car google() inclut les groupes nécessaires
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Divination"
include(":app")