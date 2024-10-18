import com.google.devtools.ksp.gradle.KspTaskMetadata
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.google.ksp)
    id("io.gitlab.arturbosch.detekt").version("1.23.7")
}

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // new repository here
}

group = "org.olafneumann.palette"
version = "1.0-SNAPSHOT"

kotlin {
    // jvm()
    js(IR) {
        browser()
    }.binaries.executable()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.fritz2.core)
                // implementation(libs.fritz2.headless) // optional
            }
        }
        /*val jvmMain by getting {
            dependencies {
            }
        }*/
        val jsMain by getting {
            dependencies {
                // tailwind
                implementation(npm(libs.tailwindcss.core))
                implementation(npm(libs.tailwindcss.typography))
                //implementation(npm(libs.tailwindcss.forms)) // optional

                // webpack
                implementation(npm(libs.postcss.core))
                implementation(npm(libs.postcss.loader))
                implementation(npm(libs.autoprefixer))
                implementation(npm(libs.css.loader))
                implementation(npm(libs.style.loader))
                implementation(npm(libs.cssnano))

                // popups
                implementation(npm("@floating-ui/dom", "1.6.11"))

                // Create downloads
                implementation(npm("file-saver", "2.0.5"))
                implementation(npm("jszip", "3.10.1"))
            }
        }
    }
}

// KSP support for Lens generation
dependencies.kspCommonMainMetadata(libs.fritz2.lenses)
kotlin.sourceSets.commonMain { tasks.withType<KspTaskMetadata> { kotlin.srcDir(destinationDirectory) } }

// FIXME: Simple workaround to make version catalogs usable for npm dependencies too. Remove if kotlin plugin
//  supports this out of the box!
fun KotlinDependencyHandler.npm(dependency: Provider<MinimalExternalModuleDependency>): Dependency =
    dependency.map { dep ->
        val name = if (dep.group == "npm") dep.name else "@${dep.group}/${dep.name}"
        npm(name, dep.version!!)
    }.get()


tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

detekt {
    // Define the detekt configuration(s) you want to use.
    config.from(file("$projectDir/.config/detekt.yml"))
    source.from(file("$projectDir/src/"))

    // Applies the config files on top of detekt's default config file. `false` by default.
    buildUponDefaultConfig = true

    // Turns on all the rules. `false` by default.
    allRules = false

    ignoreFailures = true
}

// https://kotlinlang.org/docs/js-project-setup.html#installing-npm-dependencies-with-ignore-scripts-by-default
plugins.withType<YarnPlugin> {
    rootProject.the<YarnRootExtension>().ignoreScripts = false
}

