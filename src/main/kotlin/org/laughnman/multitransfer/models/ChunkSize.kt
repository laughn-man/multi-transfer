package org.laughnman.multitransfer.models

import java.lang.RuntimeException

enum class ChunkSizeUnit(val multiplier: Long) {
	B(1), KB(1_024), MB(1_048_576), GB(1_073_741_824), TB(1_099_511_627_776)
}

fun String.toChunkSize(): ChunkSize {
	val regex = Regex("""^(\d+)((?:B|KB|MB|GB|TB)?)$""")

	return regex.matchEntire(this)
		?.let { ChunkSize(it.groupValues[1].toLong(), ChunkSizeUnit.valueOf(it.groupValues[2].takeUnless { it.isEmpty() } ?: "B")) }
		?: throw RuntimeException("$this did not match the expected format of <numeric size>B|KB|MB|GB|TB.")
}

fun Long.toChunkSize(unit: ChunkSizeUnit = ChunkSizeUnit.B) = ChunkSize(this, unit)

fun Int.toChunkSize(unit: ChunkSizeUnit = ChunkSizeUnit.B) = this.toLong().toChunkSize(unit)

data class ChunkSize(val size: Long, val unit: ChunkSizeUnit = ChunkSizeUnit.B) {

	fun toBytes() = size * unit.multiplier

	fun toUnit(newUnit: ChunkSizeUnit) = ChunkSize(toBytes() / newUnit.multiplier, newUnit)

	override fun toString(): String {
		return "$size$unit"
	}
}

class ChunkSizeConverter() : picocli.CommandLine.ITypeConverter<ChunkSize> {
	override fun convert(value: String) = value.toChunkSize()
}