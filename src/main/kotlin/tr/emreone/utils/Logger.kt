package tr.emreone.utils

import mu.KotlinLogging

object Logger {

    val logger = KotlinLogging.logger {}

    val debugLogger = KotlinLogging.logger("tr.emreone.debuglogger")

}