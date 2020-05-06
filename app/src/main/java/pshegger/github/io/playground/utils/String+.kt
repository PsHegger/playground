package pshegger.github.io.playground.utils

import java.text.Normalizer

fun String.removeDiacritics() =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
