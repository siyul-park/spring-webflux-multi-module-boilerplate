package io.github.siyual_park.auth.domain

import java.security.DigestException
import java.security.MessageDigest

fun MessageDigest.hash(value: String): String {
    val hash: ByteArray
    try {
        this.update(value.toByteArray())
        hash = this.digest()
    } catch (e: CloneNotSupportedException) {
        throw DigestException("couldn't make digest of partial content")
    }

    return bytesToHex(hash)
}

fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder()
    for (hashByte in bytes) {
        val intVal = 0xff and hashByte.toInt()
        if (intVal < 0x10) {
            sb.append('0')
        }
        sb.append(Integer.toHexString(intVal))
    }
    return sb.toString()
}
