package io.github.siyual_park.data.regexp

import java.util.regex.Pattern

abstract class Token(val value: String) {

    abstract fun convert(): String?
}

class EscapeToken(value: String) : Token(value) {
    override fun convert(): String {
        return Pattern.quote(value)
    }
}

class WildcardToken(value: String) : Token(value) {
    override fun convert(): String {
        return ".*"
    }
}

class WildcharToken(value: String) : Token(value) {
    override fun convert(): String {
        return "."
    }
}

class StringToken(value: String) : Token(value) {
    override fun convert(): String {
        return Pattern.quote(value)
    }
}
