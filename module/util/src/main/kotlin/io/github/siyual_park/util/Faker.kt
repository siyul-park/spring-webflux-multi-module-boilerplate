package io.github.siyual_park.util

import net.datafaker.Faker
import net.datafaker.Internet
import net.datafaker.Lorem
import net.datafaker.Name
import java.net.URL
import java.security.SecureRandom

private val faker = Faker(SecureRandom())

fun Name.username(size: Int? = null): String {
    var builder = StringBuilder()
    builder.append(username())
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
    builder.append(word())
    if (size == null) {
        return builder.toString()
    }

    while (builder.length < size) {
        builder = builder.append(faker.random().hex())
    }

    return builder.toString().slice(0 until size)
}

fun Internet.url(protocol: String? = null, host: String? = null, path: String? = null): URL {
    return URL("${protocol ?: "http"}://${host ?: this.domainName()}/${path ?: this.slug()}")
}
