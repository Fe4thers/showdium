import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.notations.DependencyNotationParser.create

plugins {
    id("net.fabricmc.fabric-loom")
    id("noxesium.showdium")
}
loom {
    runs {
        create("clientAuth") {
            client()
            ideConfigGenerated(true)
            //programArgs.addAll(listOf("--launch_target", "net.fabricmc.loader.impl.launch.knot.KnotClient"))
            mainClass.set("net.covers1624.devlogin.DevLogin")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    //mappings(loom.officialMojangMappings())
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    // Add DevLogin
    localRuntime(libs.devlogin)

    implementation("com.noxcrew.noxesium:fabric:3.2.3")
    //implementation("dev.isxander:debugify:26.1.2.2")
    compileOnly(files("libs/debugify-26.2.0.0.jar"))
    //compileOnly(files("libs/debugify-1.21.8+1.0.jar"))
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")

    configurations = listOf(project.configurations.runtimeClasspath.get())


    mergeServiceFiles()

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}