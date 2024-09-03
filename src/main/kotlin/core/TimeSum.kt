package core

import core.timenode.TimeTree
import kotlin.system.measureNanoTime
import core.timenode.TimeNodeState.Completed.Complete

@NonNestedDSL
class TimeSum(private val tree: TimeTree) {
	private var timeSum = 0L

	fun of(ofAction: () -> Unit) {
		timeSum += measureNanoTime { ofAction() }
	}

	fun run(title: String, action: TimeSum.() -> Unit): Long {
		tree.addTimeNode(title) {
			action()
			Complete(timeSum)
		}
		return timeSum
	}
}

