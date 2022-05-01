package org.laughnman.multitransfer.utilities

import java.io.InputStream
import java.security.MessageDigest
import java.util.*

private const val DEFAULT_BUFFER_SIZE = 8192

fun InputStream.readAsSequence(bufferSize: Int = DEFAULT_BUFFER_SIZE): Sequence<Pair<Int, ByteArray>> {
	return generateSequence {
		val buffer = ByteArray(bufferSize)
		val count = this.read(buffer, 0, bufferSize)
		if (count > -1) Pair(count, buffer)	else null
	}
}

fun ByteArray.base64Encode(): String = Base64.getEncoder().encodeToString(this)

fun InputStream.sha256Hash(): ByteArray {
	val digest = MessageDigest.getInstance("SHA-256")

	this.readAsSequence().forEach { (count, buffer) ->
		digest.update(buffer, 0, count)
	}

	return digest.digest()
}

fun ByteArray.sha256Hash(): ByteArray {
	val digest = MessageDigest.getInstance("SHA-256")
	return digest.digest(this)
}

fun ByteArray.toHex(): String = this.joinToString("") { "%02x".format(it) }
