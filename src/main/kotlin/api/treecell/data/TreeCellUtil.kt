 package api.treecell.data

import api.treecell.TreeCellState
import api.treecell.TreeCellState.Error.clone

internal fun TimeCell.toMutableTimeCell(): MutableTimeCell = convert { tCell ->
	return@convert MutableTimeCell(tCell.title).apply { this@apply._timeState = tCell.timeState.clone()  }
}

internal fun MutableTimeCell.toTimeCell()  :TimeCell = convert {mCell ->
	return@convert TimeCell(mCell.title, mCell._timeState)
}


internal fun MutableTimeCell.addTimeState(timeState: TreeCellState)  {
	when {
		this._timeState is TreeCellState.Complete && timeState is TreeCellState.Complete -> {
			(this._timeState as TreeCellState.Complete).forceSetDuration(
				(this._timeState as TreeCellState.Complete).duration + timeState.duration
			)
		}
		else -> {
			if (getPrecedence(this._timeState) < getPrecedence(timeState)) _timeState = timeState.clone()
		}
	}
}

private fun getPrecedence(state: TreeCellState): Int = when (state) {
	is TreeCellState.Error -> 0
	is TreeCellState.Invalid -> 1
	is TreeCellState.Complete -> 2
}
