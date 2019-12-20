package io.github.alxiw.spellcheckerapp

import android.text.TextUtils
import android.view.textservice.SentenceSuggestionsInfo
import java.util.HashMap

object Utils {
    @JvmStatic
    fun suggestionsToText(list: List<String?>, separator: String): String {
        val line = StringBuilder()
        for (j in list.indices) {
            if (j != 0) {
                line.append(separator)
            }
            line.append(list[j])
        }
        return line.toString()
    }

    @JvmStatic
    fun createMap(
        senInfo: SentenceSuggestionsInfo,
        numberOfWords: Int
    ): MutableMap<Int, Word?> {
        val resultMap: MutableMap<Int, Word?> = HashMap()
        for (i in 0 until numberOfWords) {
            val info = senInfo.getSuggestionsInfoAt(i)
            var list: ArrayList<String>? = null
            if (info.suggestionsCount != -1) {
                list = ArrayList()
                val numberOfSuggestions = info.suggestionsCount
                for (j in 0 until numberOfSuggestions) {
                    list.add(info.getSuggestionAt(j))
                }
            }
            resultMap[i] = Word(senInfo.getOffsetAt(i), senInfo.getLengthAt(i), list)
        }
        return resultMap
    }

    @JvmStatic
    fun isMapNotEmpty(map: MutableMap<Int, Word?>, numberOfWords: Int): Boolean {
        var isNotEmpty = false
        for (i in 0 until numberOfWords) {
            if (map.containsKey(i)) {
                val word = map[i]
                word?.let {
                    it.suggestions?.let { list ->
                        if (isListNotEmpty(list)) {
                            isNotEmpty = true
                        }
                    }
                }
            }
        }
        return isNotEmpty
    }

    @JvmStatic
    fun isListNotEmpty(list: ArrayList<String>): Boolean {
        for (i in 0 until list.size) {
            if (!TextUtils.isEmpty(list[i])) {
                return true
            }
        }
        return false
    }
}