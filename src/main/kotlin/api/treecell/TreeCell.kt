package api.treecell

@Suppress("UNCHECKED_CAST")
interface TreeCell<T : TreeCell<T>> {
	val title: String
	var children: List<T>

	fun flatten(traversalType: TraversalType = TraversalType.PREORDER): List<T> =
		traversRec(this as T , mutableListOf(), traversalType)

	fun toMap(unTrimmed: Boolean = false): Map<String, T> {
		val map = mutableMapOf<String, T>()
		mapRec(this as T, map, unTrimmed)
		return map
	}

	enum class TraversalType {
		PREORDER, POSTORDER
	}

	private fun traversRec(
		node: T, list: MutableList<T>, type: TraversalType
	): List<T> {
		if (type == TraversalType.PREORDER) list.add(node)
		if (node.children.isNotEmpty()) {
			node.children.forEach {
				traversRec(it, list, type)
			}
		}
		if (type == TraversalType.POSTORDER) list.add(node)
		return list
	}

	private fun mapRec(node: T, map: MutableMap<String, T>, unTrimmed: Boolean) {
		if (unTrimmed || node.children.isNotEmpty()) map[node.title] = node
		node.children.forEach { mapRec(it, map, unTrimmed) }
	}

	fun <R : TreeCell<R>> convert(action: (T) -> R): R {
		val tCell = this as T
		val rCell = action(tCell)
		recConvert(tCell, rCell, action)
		return rCell
	}

	private fun  <R : TreeCell<R>>  recConvert(tCell: T, rCell: R, action: (T) -> R) {
		rCell.children = tCell.children.map { action(it) }
		tCell.children.zip(rCell.children) { tChild, rChild ->
			recConvert(tChild, rChild, action)
		}
	}

}
