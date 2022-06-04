package io.github.siyual_park.util

import com.github.javafaker.Internet
import com.github.javafaker.Lorem
import com.github.javafaker.Name
import java.net.URL

fun Name.username(size: Int? = null): String {
    if (size == null) {
        return username()
    }

    var builder = StringBuilder()
    while (builder.length < size) {
        builder = builder.append(username())
    }

    return builder.toString().slice(0 until size)
}

fun Lorem.word(size: Int? = null): String {
    if (size == null) {
        return word()
    }

    var builder = StringBuilder()
    while (builder.length < size) {
        builder = builder.append(word())
    }

    return builder.toString().slice(0 until size)
}

fun Internet.url(host: String? = null, path: String? = null, scheme: String? = null): URL {
    return URL("${scheme ?: "http"}://${host ?: this.domainName()}/${path ?: this.slug()}")
}
