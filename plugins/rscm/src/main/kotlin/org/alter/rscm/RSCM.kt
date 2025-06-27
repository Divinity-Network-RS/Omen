package org.alter.rscm

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

/**
 * @author Cl0udS3c
 */
object RSCM {
    private var rscmList = mutableMapOf<String, Int>()
    val logger = KotlinLogging.logger {}
    fun getRSCM(entity: Array<String>): List<Int> = entity.map { getRSCM(it) }.toList()
    fun Int.asRSCM(table: String): String {
        return rscmList.entries.find { it.value == this && it.key.startsWith("$table.") }?.key
            ?: throw IllegalStateException("No RSCM entry found for ID $this with prefix '$table'.")
    }
    fun getRSCM(entity: String) : Int {
        if (rscmList.isEmpty()) {
            throw IllegalStateException("RSCM List is empty.")
        }
        var result = rscmList[entity] ?: -1
        if (result == -1) {
            throw IllegalStateException("RSCM returned -1 for $entity.")
        }
        return result
    }

    fun init() {
        initRSCM()
        logger.info { "RSCM Loaded" }
    }

    fun initRSCM() {
        Path.of("../data/cfg/rscm/").toFile().listFiles()?.forEach {
            val map = it.name.replace(".rscm", "")
            it.bufferedReader(Charsets.UTF_8).use { buff ->
                buff.lineSequence().forEach { line ->
                    val divider = line.split(":")
                    if (divider.size == 2) {
                        val key = "$map." + divider[0].trim()
                        val value = divider[1].trim().toInt()
                        rscmList[key] = value
                    } else {
                        println("$line not enough arguments")
                    }
                }
            }
        }
    }

    fun test() {
        val expectations = mapOf(
            "quest.dwarf_remains" to 0,
            "quest.toolkit" to 1,
            "quest.cannonball" to 2,
            "quest.insect_repellent_noted" to 29
        )

        var passed = 0
        var failed = 0

        for ((key, expectedValue) in expectations) {
            try {
                val actualValue = getRSCM(key)
                if (actualValue == expectedValue) {
                    logger.info { "PASS: $key = $actualValue" }
                    passed++
                } else {
                    logger.error { "FAIL: $key expected $expectedValue but got $actualValue" }
                    failed++
                }
            } catch (e: Exception) {
                logger.error(e) { "ERROR: $key threw exception" }
                failed++
            }
        }

        logger.info { "RSCM Test Complete: $passed passed, $failed failed." }
    }

}