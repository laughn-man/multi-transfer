package org.laughnman.multitransfer.dao

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

interface FileDao {

	fun getLength(file: File): Long

	fun isFile(file: File): Boolean

	fun isDirectory(file: File): Boolean

	fun delete(file: File): Boolean

	fun openForRead(file: File): InputStream

	fun openForWrite(file: File): OutputStream
	fun openReadChannel(file: File): ReadableByteChannel

	fun openWriteChannel(file: File): WritableByteChannel

}