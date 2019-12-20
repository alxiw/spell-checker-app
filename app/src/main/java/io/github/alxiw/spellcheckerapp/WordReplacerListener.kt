package io.github.alxiw.spellcheckerapp

interface WordReplacerListener {
    fun replaceWord(text: String?, offset: Int, length: Int)
}