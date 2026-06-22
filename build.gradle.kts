plugins {
    id("java")
}

group = "me.casinocore"
version = "3.0"

data class BuildVariant(
    val mainClass: String,
    val playUsage: String = "/play <game> <bet>, /play dice <bet> [low|medium|high], /play coinflip <create|join|cancel|list>, or /play list"
)

val mainVariant = BuildVariant(
    mainClass = "com.casinocore.core.CasinoCore"
)

fun BuildVariant.resourceProperties(): Map<String, String> = mapOf(
    "project.version" to project.version.toString(),
    "build.main.class" to mainClass,
    "build.play.usage" to playUsage
)

fun org.gradle.api.file.CopySpec.applyResourcePlaceholders(props: Map<String, String>) {
    filesMatching(listOf("plugin.yml", "config.yml")) {
        filter { line ->
            props.entries.fold(line) { current, entry ->
                current.replace("\${${entry.key}}", entry.value)
            }
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.10")
    compileOnly("net.citizensnpcs:citizensapi:2.0.42-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.42-SNAPSHOT") {
        isTransitive = false
    }

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

/* =========================
   TESTS
========================= */
tasks.test {
    useJUnitPlatform()
}

/* =========================
   MAIN BUILD (DEFAULT)
========================= */
tasks.processResources {
    val props = mainVariant.resourceProperties()
    inputs.properties(props)
    filteringCharset = "UTF-8"
    applyResourcePlaceholders(props)
}
