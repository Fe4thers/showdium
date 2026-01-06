import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
    id("maven-publish")
    id("noxesium.showdium")
}



dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")

    compileOnly("com.noxcrew.noxesium:paper-platform:3.0.0-rc.1")
}