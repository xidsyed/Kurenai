package api.timecell

import api.timecell.TraversalType.POSTORDER
import api.timecell.TraversalType.PREORDER

/**
 * Returns a list of all nodes in the tree, in the order specified by the traversal type.
 * `TraversalType.PREORDER` is the default and is the order that time nodes were visited.
 * `TraversalType.POSTORDER` is the order that time nodes were complete.
 * @param traversalType the order in which the nodes are returned
 * */

fun TimeCell.flatten(traversalType: TraversalType = PREORDER): List<TimeCell> =
	traversRec(this, mutableListOf(), traversalType)

/**
 * Returns a pre-ordered map of node and its children list .
 * @param unTrimmed if true, only nodes with children will be included in the list
 * */
fun TimeCell.toMap(unTrimmed: Boolean = false): Map<TimeCell, List<TimeCell>> {
	val map = mutableMapOf<TimeCell, List<TimeCell>>()
	mapRec(this, map, unTrimmed)
	return map
}

/**
 * @property PREORDER Calling Order
 * @property POSTORDER Completion Order
 * */
enum class TraversalType {
	PREORDER, POSTORDER/**/
}


private fun traversRec(
	node: TimeCell, list: MutableList<TimeCell>, type: TraversalType
): List<TimeCell> {
	if (type == PREORDER) list.add(node)
	if (node.children.isNotEmpty()) {
		node.children.forEach {
			traversRec(it, list, type)
		}
	}
	if (type == POSTORDER) list.add(node)
	return list
}


private fun mapRec(node: TimeCell, list: MutableMap<TimeCell, List<TimeCell>>, unTrimmed: Boolean) {
	if (unTrimmed || !unTrimmed && node.children.isNotEmpty()) list[node] = node.children
	node.children.forEach { mapRec(it, list, unTrimmed) }
}


