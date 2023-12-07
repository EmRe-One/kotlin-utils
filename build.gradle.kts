plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://jcenter.bintray.com")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19

    withSourcesJar()
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    // Do not use, no effect; will be overridden by kotlinDslPluginOptions.jvmTarget, see KotlinDslCompilerPlugins.
    kotlinOptions.jvmTarget = JavaVersion.VERSION_19.toString()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("ch.qos.logback:logback-core:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.github.ajalt.mordant:mordant:2.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.yaml:snakeyaml:2.2")

    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    testImplementation(kotlin("test"))
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
}

publishing {
    repositories {
        maven {
            name = "Kotlin-Utils"
            url = uri("https://maven.pkg.github.com/Emre-One/kotlin-utils")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "tr.emreone"
            artifactId = "kotlin-utils"
            from(components["java"])
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.register("prepareNextDay") {
    var day = 0
    var packageId = ""

    doFirst {
        day = properties["day"]?.toString()?.toInt() ?: 0
        packageId = properties["packageId"]?.toString() ?: "tr.emreone.adventofcode"
    }

    doLast {
        val nextDay = day.toString().padStart(2, '0')
        val withTest = true
        val packageIdPath = packageId.replace(".", "/")

        val mainFile = "${projectDir}/src/main/kotlin/${packageIdPath}/Main.kt"
        val readmeFile = "${projectDir}/README.md"
        val newSrcFile = "${projectDir}/src/main/kotlin/${packageIdPath}/days/Day${nextDay}.kt"
        val newTestFile = "${projectDir}/src/test/kotlin/${packageIdPath}/days/Day${nextDay}Test.kt"

        if (file(newSrcFile).exists()) {
            println("WARNING: Files for Day$nextDay already exists. Do you really want to overwrite it?")
        } else {
            file(newSrcFile).writeText(
                file("${projectDir}/template/DayX.kt")
                    .readText()
                    .replace("$1", nextDay)
            )

            file("${projectDir}/src/main/resources/day${nextDay}.txt")
                .writeText("")

            file(mainFile).writeText(
                file(mainFile).readText()
                    .replace(
                        "// $1", """
                        |    fun solveDay${nextDay}() {
                        |        val input = Resources.resourceAsList(fileName = "day${nextDay}.txt")
                        |
                        |        val solution1 = Day${nextDay}.part1(input)
                        |        logger.info { "Solution1: ${"$"}solution1" }
                        |
                        |        val solution2 = Day${nextDay}.part2(input)
                        |        logger.info { "Solution2: ${"$"}solution2" }
                        |    }
                        |// ${"$1"}
                        """.trimMargin()
                    )
            )

            file(readmeFile).writeText(
                file(readmeFile).readText()
                    .replace(
                        "<!-- $1 -->", """
                            |[Day ${nextDay}](https://adventofcode.com/2022/day/${nextDay}) | [Day${nextDay}Test.kt](https://github.com/EmRe-One/advent-of-code-2022/blob/master/src/test/kotlin/tr/emreone/adventofcode/days/Day${nextDay}Test.kt) | [Day${nextDay}.kt](https://github.com/EmRe-One/advent-of-code-2022/blob/master/src/main/kotlin/tr/emreone/adventofcode/days/Day${nextDay}.kt) |       |       |
                            ${"<!-- $1 -->"}
                        """.trimIndent()
                    )
            )

            if (withTest) {
                file(newTestFile).writeText(
                    file("${projectDir}/template/DayXTest.kt")
                        .readText()
                        .replace("$1", nextDay)
                )

                file("${projectDir}/src/test/resources/day${nextDay}_example.txt")
                    .writeText("")
            }
        }
    }
}
