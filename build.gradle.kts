import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
}

group = "dev.roman.kamyshnikov.codegen"
version = "0.1.0"

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

private object TargetIde {
    const val TYPE = "IC" // IC - IntelliJ Community, AI - Android Studio
    const val VERSION = "2025.1.2" // Narwhal 3 Feature Drop 2025.1.3
    const val BUILD = "251.26094.121"
    const val BUILD_MAX = "251.*"
}

dependencies {
    intellijPlatform {
        // Android Studio releases -> https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
        create(type = TargetIde.TYPE, version = TargetIde.VERSION)

        // Plugin dependencies -> https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html
        bundledPlugins(listOf("org.jetbrains.kotlin", "com.intellij.java"))
        plugins(emptyList())
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "dev.roman.kamyshnikov.codegen"
        name = "Codegen"
        vendor {
            email = "rkam88@gmail.com"
            url = "https://www.linkedin.com/in/roman-kam/"
        }
        description = """
            Data layer code generation. R-click on any package and select the API function to generate code for.<br>
            <br>
            <b>Plugin behaviour:</b><br>
            - Clones data and enum classes used as input parameters and return type for the selected API function.<br>
            <br>
            <b>Conflict resolution strategy:</b><br>
            - data classes: if a class with the same name, but different constructor parameters exists in the same directory - creates the class with a "_NEW" suffix.<br>
            - enum classes: if a class with the same name, but different constructor parameters or enum entries exists in the same directory - creates the class with a "_NEW" suffix.<br>
            <br>
        """.trimIndent()
        changeNotes = """
            <b>Changes:</b><br>
            - Initial release.<br>
        """.trimIndent()

        // Supported build number ranges -> https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        ideaVersion {
            sinceBuild = TargetIde.BUILD
            untilBuild = TargetIde.BUILD_MAX
        }
    }
}

tasks.named<RunIdeTask>("runIde") {
    jvmArgumentProviders += CommandLineArgumentProvider {
        listOf("-Didea.kotlin.plugin.use.k2=true")
    }
}

tasks.test {
    jvmArgumentProviders += CommandLineArgumentProvider {
        listOf("-Didea.kotlin.plugin.use.k2=true")
    }
}

val runLocalIde by intellijPlatformTesting.runIde.registering {
    localPath = file("/Applications/Android Studio.app/Contents")
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf("-Didea.kotlin.plugin.use.k2=true")
        }
    }
}
