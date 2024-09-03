package api.timecell

import api.timecell.NanosConverter.NANOS_IN_DAY
import api.timecell.NanosConverter.NANOS_IN_HOUR
import api.timecell.NanosConverter.NANOS_IN_MILLI
import api.timecell.NanosConverter.NANOS_IN_MINUTE
import api.timecell.NanosConverter.NANOS_IN_SECOND
import core.timenode.TimeNode
import core.timenode.TimeNodeState

data class TimeCell(
	val title: String, val timeState: TimeCellState, var children: List<TimeCell> = emptyList()
) {
	val duration: Long?
		get() = if (timeState is TimeCellState.Complete) timeState.duration else null
	val formattedTime by lazy { duration?.let { FormattedTime(it) } }

	override fun toString() = "$title - " + (formattedTime?.let { "${it.timeInMillis}ms" } ?: "Invalid")
}

class FormattedTime(
	private val duration: Long,
) {
	val timeInMillis: Long
		get() = duration / NANOS_IN_MILLI

	val timeInSeconds: Long
		get() = duration / NANOS_IN_SECOND

	val timeInMinutes: Long
		get() = duration / NANOS_IN_MINUTE

	val timeInHours: Long
		get() = duration / NANOS_IN_HOUR

	val timeInDays: Long
		get() = duration / NANOS_IN_DAY

	/*
	val formattedTime by lazy {
		val sb = StringBuilder()
		var duration = nano
		if (duration > NANOS_IN_DAY) {
			sb.append("${duration / NANOS_IN_DAY}d")
			duration %= NANOS_IN_DAY
		}

		if (duration > NANOS_IN_HOUR) {
			sb.append("${(duration / NANOS_IN_HOUR).toInt}h")
			duration %= NANOS_IN_HOUR
		}

		sb.toString()
	}
	*/

}

sealed interface TimeCellState {
	object Invalid : TimeCellState

	@JvmInline
	value class Complete(val duration: Long) : TimeCellState
}


object NanosConverter {
	val NANOS_IN_DAY = 86_400_000_000_000
	val NANOS_IN_HOUR = 3_600_000_000_000
	val NANOS_IN_MINUTE = 60_000_000_000
	val NANOS_IN_SECOND = 1_000_000_000
	val NANOS_IN_MILLI = 1_000_000
	val NANOS_IN_MICRO = 1_000

}

fun TimeNode.toTimeCell(): TimeCell = TimeCell(
	title = title,
	timeState = timeState.let {
		if (it is TimeNodeState.Completed.Complete) TimeCellState.Complete(it.time) else TimeCellState.Invalid
	},
	children = children.toTimeCells()
)

fun List<TimeNode>.toTimeCells() = map { it.toTimeCell() }