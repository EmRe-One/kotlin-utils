package tr.emreone.utils.extensions

fun String.parse(pattern: String): List<String> {
    val regex = pattern.toRegex()
    val matchResult = regex.matchEntire(this)
    return matchResult?.destructured?.toList() ?: emptyList()
}
