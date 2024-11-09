package core.parser

import api.treecell.data.TimeCell



class IterationHolder(val rootCell: TimeCell) {
	val titleMap: Map<String, TimeCell>
	val callOrderedList: List<TimeCell>
	val completionOrderedList: List<TimeCell>

	init {
		val triple = mapPrePostListFromRoot(rootCell)
		titleMap = triple.first
		callOrderedList = triple.second
		completionOrderedList = triple.third
	}
}


fun mapPrePostListFromRoot(rootTimeCell: TimeCell): Triple<Map<String, TimeCell>, List<TimeCell>, List<TimeCell>> {
	val preList: MutableList<TimeCell> = mutableListOf()
	val postList: MutableList<TimeCell> = mutableListOf()
	val map: MutableMap<String, TimeCell> = mutableMapOf()

	rec(rootTimeCell, map, preList, postList )
	return Triple(map, preList, postList)
}

private fun rec(
	node: TimeCell,
	map: MutableMap<String, TimeCell>,
	preList: MutableList<TimeCell>,
	postList: MutableList<TimeCell>,
) {
	preList.add(node)
	map[node.title] = node
	if (node.children.isNotEmpty()) {
		node.children.forEach {
			rec(it, map, preList, postList)
		}
	}
	postList.add(node)
}

enum class IterationListOrder { CALL_ORDER, COMPLETION_ORDER }