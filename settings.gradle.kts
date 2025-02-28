// ------------------------------------ Plugin Management ------------------------------------
// This section defines repositories used for resolving Gradle plugins.
pluginManagement {
    repositories {
        google()                // Google's Maven repository
        mavenCentral()          // Maven Central repository
        gradlePluginPortal()    // Gradle Plugin Portal
        maven { url = uri("https://maven.google.com") } // Additional Google Maven repository
    }
}

// ------------------------------------ Dependency Management ------------------------------------
// This section defines repositories used for resolving project dependencies.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()                // Google's Maven repository
        mavenCentral()          // Maven Central repository
        maven { url = uri("https://maven.google.com") } // Ensure this repository is included
    }
}

// ------------------------------------ Project Configuration ------------------------------------
// Define the root project name and include sub-projects/modules.
rootProject.name = "ISENSmartCompanion"
include(":app")  // Include the main app module
