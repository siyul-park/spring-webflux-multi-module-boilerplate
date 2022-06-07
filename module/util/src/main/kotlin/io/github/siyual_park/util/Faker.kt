package io.github.siyual_park.util

import com.github.javafaker.Faker
import com.github.javafaker.Internet
import com.github.javafaker.Lorem
import com.github.javafaker.Name
import java.net.URL

private val faker = Faker()

fun Name.username(size: Int? = null): String {
    var builder = StringBuilder()
    builder = builder.append(username())

    if (size == null) {
        return builder.toString()
    }

    while (builder.length < size) {
        builder = builder.append(faker.random().hex())
    }

    return builder.toString().slice(0 until size)
}

fun Lorem.word(size: Int? = null): String {
    var builder = StringBuilder()
    builder = builder.append(word())

    if (size == null) {
        return builder.toString()
    }

    while (builder.length < size) {
        builder = builder.append(faker.random().hex())
    }

    return builder.toString().slice(0 until size)
}

fun Internet.url(host: String? = null, path: String? = null, scheme: String? = null): URL {
    return URL("${scheme ?: "http"}://${host ?: this.domainName()}/${path ?: this.slug()}")
}
