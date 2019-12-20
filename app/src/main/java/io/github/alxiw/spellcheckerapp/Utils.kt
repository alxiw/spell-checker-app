package io.github.alxiw.spellcheckerapp

object Utils {
    @JvmStatic
    fun suggestionsToText(list: List<String?>): String {
        val line = StringBuilder()
        for (j in list.indices) {
            if (j != 0) {
                line.append(", ")
            }
            line.append(list[j])
        }
        return line.toString()
    }
}