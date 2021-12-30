plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("ch.qos.logback:logback-core:1.2.10")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

java {
    withSourcesJar()
    withJavadocJar()
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
            from(components["java"])
        }
    }
}

// TODO call task with parameters for nextDay and PackageIdPath
tasks.register("prepareNextDay") {
    doLast {
        val nextDay = 0
        val withTest = true
        val packageIdPath = "de.emreak.adventofcode".replace(".", "/")

        val mainFile    = """$projectDir/src/main/kotlin/${packageIdPath}/Main.kt"""
        val readmeFile  = """$projectDir/README.md"""
        val newSrcFile  = """$projectDir/src/main/kotlin/${packageIdPath}/days/Day${nextDay}.kt"""
        val newTestFile = """$projectDir/src/test/kotlin/${packageIdPath}/days/Day${nextDay}Test.kt"""

        if (file(newSrcFile).exists()) {
            println("WARNING: Files for Day$nextDay already exists. Do you really want to overwrite it?")
        } else {
            file(newSrcFile).writeText(
                file("""$projectDir/template/DayX.kt""")
                    .readText()
                    .replace("$1", "$nextDay")
            )
            file("""$projectDir/src/main/resources/day$nextDay.txt""").writeText("")

            file(mainFile).writeText(
                file(mainFile).readText()
                    .replace(
                        "// $1", """
                            $nextDay -> solveDay${nextDay}()
                            // ${"$1"} 
                        """.trimIndent()
                    )
                    .replace(
                        "// $2", """
                        fun solveDay${nextDay}() {
                            val input = Resources.resourceAsList("day${nextDay}.txt")
    
                            val solution1 = Day${nextDay}.part1(input)
                            logger.info { "Solution1: ${"$"}solution1" }
    
                            val solution2 = Day${nextDay}.part2(input)
                            logger.info { "Solution2: ${"$"}solution2" }
                        }
                        
                        // ${"$2"}
                        """.trimIndent()
                    )
            )

            file(readmeFile).writeText(
                file(readmeFile).readText()
                    .replace(
                        "<!-- $1 -->",
                        """| [Day ${nextDay}](https://adventofcode.com/2021/day/${nextDay}) | [Day${nextDay}Test.kt](https://github.com/EmRe-One/advent-of-code-2021/blob/master/src/test/kotlin/de/emreak/adventofcode/days/Day${nextDay}Test.kt) | [Day${nextDay}.kt](https://github.com/EmRe-One/advent-of-code-2021/blob/master/src/main/kotlin/de/emreak/adventofcode/days/Day${nextDay}.kt) |       |       |
${"<!-- $1 -->"}
                        """.trimIndent()
                    )
            )

            if (withTest) {
                file(newTestFile).writeText(
                    file("""$projectDir/template/DayXTest.kt""")
                        .readText()
                        .replace("$1", "$nextDay")
                )

                file("""$projectDir/src/test/resources/day${nextDay}_example.txt""")
                    .writeText("")
            }
        }
    }
}
