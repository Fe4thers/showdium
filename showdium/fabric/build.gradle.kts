import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.notations.DependencyNotationParser.create

plugins {
    id("fabric-loom")
    id("noxesium.showdium")
}
loom {

    runs {
        create("clientAuth") {
            client()
            ideConfigGenerated(true)
            programArgs.addAll(listOf("--launch_target", "net.fabricmc.loader.impl.launch.knot.KnotClient"))
            mainClass.set("net.covers1624.devlogin.DevLogin")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    // Add DevLogin
    localRuntime(libs.devlogin)

    // Rely on the Noxesium Fabric mod implementation as another mod, here because it's in the
    // same repository it's using this custom syntax, but you can use modImplementation!
    modImplementation("com.noxcrew.noxesium:fabric:3.0.0-rc.1")
    implementation("dev.isxander:debugify:1.21.10+1.1")
    //compileOnly(files("libs/debugify-1.21.8+1.0.jar"))
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("") // make the output replace normal jar

    // Include runtime dependencies (modImplementation, localRuntime)
    configurations = listOf(project.configurations.runtimeClasspath.get())

    // Optionally relocate packages to avoid conflicts
    // relocate("com.noxcrew", "your.shadowed.noxcrew")

    mergeServiceFiles() // merge any service files inside dependencies

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // or INCLUDE/MERGE based on your needs
}