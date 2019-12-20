package io.github.alxiw.spellcheckerapp

class Word internal constructor(
    var offset: Int,
    var length: Int,
    var suggestions: ArrayList<String>?
)