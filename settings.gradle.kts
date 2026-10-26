pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()   // NUEVO: busca tambien en ~/.m2 (donde quedo gastos-core con "mvn install")
    }
}
rootProject.name = "ControlGastos"
include(":app")
