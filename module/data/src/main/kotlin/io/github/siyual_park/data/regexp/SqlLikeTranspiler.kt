package io.github.siyual_park.data.regexp

object SqlLikeTranspiler {
    private val TOKENIZER = Tokenizer()
        .add("^\\[([^]]*)]") { EscapeToken(it) }
        .add("^(%)") { WildcardToken(it) }
        .add("^(_)") { WildcharToken(it) }
        .add("^([^\\[\\]%_]+)") { StringToken(it) }

    fun toRegEx(pattern: String): String {
        val sb = StringBuilder().append("^")
        val tokens = TOKENIZER.tokenize(pattern)
        for (token in tokens) {
            sb.append(token.convert())
        }
        return sb.append("$").toString()
    }
}
