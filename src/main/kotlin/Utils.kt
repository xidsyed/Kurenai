import java.time.LocalTime

internal fun convertNanoToMilli(nano: Long): Double = nano / 1_000_000.0

// Function to convert duration in nanoseconds to a formatted time string
internal fun Long.nanoToTimeString(): String {
	val time = LocalTime.ofNanoOfDay(this)
	return "${time.hour}hr ${time.minute}m ${time.second}s ${time.nano/1_000_000}ms ${this}ns"
}

object Conversions {
}