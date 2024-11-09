package api.treecell.data

import api.treecell.*
import api.treecell.TreeCellState.Error

internal class MutableTimeCell(override val title: String) : TreeCell<MutableTimeCell> {
	var _timeState: TreeCellState = Error
	override var children: List<MutableTimeCell> = emptyList()
}
