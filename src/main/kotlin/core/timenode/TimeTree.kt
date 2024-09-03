package core.timenode

class TimeTree(val name: String) {
	var rootNode = TimeNode(name, null)
	var currentNode = rootNode

	/**
	 * Resets the rootNode to a new TimeNode and sets the currentNode to the rootNode.
	 * */
	fun resetTree() {
		rootNode = TimeNode(name, null)
		currentNode = rootNode
	}

	/**
	 * Creates a new TimeNode with the given title, sets it as the current node and returns it.
	 * The node is created in its appropriate position in the tree.
	 * @param title The title of the new node
	 * @return The new
	 * */
	fun incCurrentNode(title: String): TimeNode {
		currentNode = currentNode.next(title)
		return currentNode
	}

	fun addTimeNode(title: String, timedNode: () -> TimeNodeState.Completed) {
		val currentNode = incCurrentNode(title)
		val nodeTime = timedNode()
		currentNode.complete(nodeTime)
	}

}