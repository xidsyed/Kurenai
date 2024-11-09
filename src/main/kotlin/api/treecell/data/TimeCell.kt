package api.treecell.data

import api.treecell.*
import core.timenode.*

class TimeCell(
	override val title: String,
	val timeState: TreeCellState,
	override var children: List<TimeCell> = emptyList()
) : TreeCell<TimeCell> {
	val duration: Long?
		get() = if (timeState is TreeCellState.Complete) timeState.duration else null

	override fun toString(): String = duration?.let { "$title : ${duration}ns\n" } ?: "$title : $timeState"
}

fun TimeNode.toTimeCell(): TimeCell = TimeCell(
	title = title,
	timeState = timeState.let {
		when (it) {
			is TimeNodeState.Completed.Complete -> TreeCellState.Complete(it.time)
			is TimeNodeState.Completed.Error -> TreeCellState.Error
			else -> TreeCellState.Invalid
		}
	},
	children = children.toTimeCells()
)

fun List<TimeNode>.toTimeCells() = map { it.toTimeCell() }
