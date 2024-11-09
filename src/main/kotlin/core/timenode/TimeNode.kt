package core.timenode

class TimeNode(val title: String, private val parent: TimeNode?) {

	var timeState: TimeNodeState = TimeNodeState.Active
		private set(value) {
			if (timeState is TimeNodeState.Completed)
				throw Exception("This time node is complete. time cannot be re-assigned")
			field = value
		}
	val children: MutableList<TimeNode> = mutableListOf()

	fun complete(duration: Long): Long {
		timeState = TimeNodeState.Completed.Complete(duration)
		return duration
	}

	fun complete(state: TimeNodeState.Completed){
		timeState = state
	}


	fun next(title: String): TimeNode {
		val next = if (timeState is TimeNodeState.Active) {
			val child = TimeNode(title, this)
			children.add(child)
			child
		} else {
			val nextParent = this.findNextParent()
			val sibling = TimeNode(title, nextParent)
			nextParent.children.add(sibling)
			sibling
		}
		return next
	}

	private fun findNextParent(): TimeNode {
		try {
			var parentNode = this.parent!!
			while (parentNode.timeState is TimeNodeState.Completed) {
				parentNode = parentNode.parent!!
			}
			return parentNode

		} catch (e: NullPointerException) {
			throw NullPointerException("Root Node has no Parent!")
		}
	}

}

sealed interface TimeNodeState {
	object Active : TimeNodeState
	sealed interface Completed : TimeNodeState {
		@JvmInline
		value class Complete(val time: Long) : Completed
		object Invalid : Completed
		object Error : Completed
	}
}


