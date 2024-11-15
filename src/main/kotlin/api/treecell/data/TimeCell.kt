package api.treecell.data

import api.treecell.*
import core.timenode.*
import reporter.utils.UnitOfTime

class TimeCell(
	override val title: String, val timeState: TreeCellState, override var children: List<TimeCell> = emptyList()
) : TreeCell<TimeCell> {
	fun getDuration(unit: UnitOfTime = UnitOfTime.NANOSECOND): Long? {
		return if (timeState is TreeCellState.Complete) timeState.duration.div(unit.inNanos)
		else null
	}

	override fun toString(): String = getDuration()?.let { "$title : ${getDuration()}ns\n" } ?: "$title : $timeState"
}

fun TimeNode.toTimeCell(): TimeCell = TimeCell(
	title = title, timeState = timeState.let {
		when (it) {
			is TimeNodeState.Completed.Complete -> TreeCellState.Complete(it.time)
			is TimeNodeState.Completed.Error -> TreeCellState.Error
			else -> TreeCellState.Invalid
		}
	}, children = children.toTimeCells()
)

class Duration(private val _value: Long, var unit: UnitOfTime = UnitOfTime.NANOSECOND) {
	val value: Long get() = _value.div(unit.inNanos)
	override fun toString(): String {
		return "$value${unit.suffix}"
	}
}

fun List<TimeNode>.toTimeCells() = map { it.toTimeCell() }
