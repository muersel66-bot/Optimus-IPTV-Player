package com.iptvplayerpro.data

/** Parses a standard #EXTM3U playlist (as used by most IPTV providers). */
object M3uParser {
    private val attr = Regex("""([\w-]+)="([^"]*)"""")

    fun parse(text: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name: String? = null
        var group = ""
        var logo: String? = null

        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val attrs = attr.findAll(line).associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    group = attrs["group-title"].orEmpty()
                    logo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    name = line.substringAfterLast(',', "").trim()
                        .ifEmpty { attrs["tvg-name"].orEmpty() }
                }
                line.isEmpty() || line.startsWith("#") -> Unit
                else -> {
                    result += Channel(
                        number = result.size + 1,
                        name = name?.ifEmpty { null } ?: "Channel ${result.size + 1}",
                        group = group.ifEmpty { "Other" },
                        logo = logo,
                        url = line,
                    )
                    name = null; group = ""; logo = null
                }
            }
        }
        return result
    }
}
