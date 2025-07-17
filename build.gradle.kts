plugins {
    kotlin("jvm") version "2.2.20-Beta1"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

var mcVer = providers.gradleProperty("minecraftVersion").orNull ?: throw Error("shreddedPaperBuild must be defined in gradle.properties")
var buildNum = providers.gradleProperty("shreddedPaperBuild").orNull ?: throw Error("shreddedPaperBuild must be defined in gradle.properties")
group = providers.gradleProperty("group").orNull ?: throw Error("group must be defined in gradle.properties")
version = providers.gradleProperty("version").orNull ?: throw Error("version must be defined in gradle.properties")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.clojars.org/")
}

dependencies {
    compileOnly("com.github.puregero:shreddedpaper-api:${mcVer}-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:${mcVer}-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.22.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    runServer {
        var url = "https://api.multipaper.io/v2/projects/shreddedpaper/versions/${mcVer}/builds/${buildNum}/downloads/shreddedpaper-${mcVer}-${buildNum}.jar"
        var jarPath = "./server/shreddedpaper-${mcVer}-${buildNum}.jar"

        if(!File(jarPath).exists()) {
            println("Downloading to shredded paper to ${jarPath}")
            File("./server").mkdirs()
            download(url, jarPath)
        }

        serverJar(File(jarPath))
        minecraftVersion(mcVer)
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

fun download(url : String, path : String){
    val destFile = File(path)
    ant.invokeMethod("get", mapOf("src" to url, "dest" to destFile))
}

