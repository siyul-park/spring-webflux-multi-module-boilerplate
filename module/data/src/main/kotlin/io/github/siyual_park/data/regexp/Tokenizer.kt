package io.github.siyual_park.data.regexp

import java.text.ParseException
import java.util.regex.Pattern

class Tokenizer {
    private val patterns = mutableListOf<Pair<Pattern, (String) -> Token>>()

    fun add(regex: String, creator: (String) -> Token): Tokenizer {
        patterns.add(Pattern.compile(regex) to creator)
        return this
    }

    fun tokenize(clause: String): Collection<Token> {
        val tokens = mutableListOf<Token>()
        var copy = String(clause.toCharArray())
        var position = 0
        while (copy != "") {
            var found = false
            for ((pattern, fn) in patterns) {
                val m = pattern.matcher(copy)
                if (m.find()) {
                    found = true
                    val token = m.group(1)
                    tokens.add(fn(token))
                    copy = m.replaceFirst("")
                    position += token.length
                    break
                }
            }
            if (!found) {
                throw ParseException("Unexpected sequence found in input string.", ++position)
            }
        }
        return tokens
    }
}
