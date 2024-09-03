package api

import api.timecell.TimeCell
import api.timecell.TimeCellState
import api.timecell.TraversalType
import api.timecell.flatten

/**
 * Container for the results of a benchmark
 * @param title: The title of the benchmark
 * @param config: The configuration used for the benchmark
 * @param iterations: The results of the benchmark
 * @param warmupIterations: The results of the warmup runs
 * @param benchmarkTime: The total time taken to run the benchmark
 * */
class BenchmarkResult(
	val title: String,
	val config: BenchmarkConfig,
	val iterations: List<TimeCell>,
	val warmupIterations: List<TimeCell>,
	val benchmarkTime: Long,
) {
	private val iterationListsPreorder by lazy { iterations.map { it.flatten(traversalType = TraversalType.PREORDER) } }
	private val iterationListsPostorder by lazy { iterations.map { it.flatten(traversalType = TraversalType.POSTORDER) } }

	val averagedTimeCells by lazy {
		val titles = iterationListsPreorder.first().map { it.title }
		val count = titles.size
		val sumOfTimes = LongArray(count)
		iterationListsPreorder.forEach {
			//it.forEachIndexed { index, timeNode -> timeNode.time?.let { sumOfTimes[index] += it } }
		}

		val averageTimeCellList: MutableList<TimeCell> = mutableListOf()
		titles.forEachIndexed { index, title ->
			averageTimeCellList.add(TimeCell(title, TimeCellState.Complete(sumOfTimes[index] / count)))
		}
		averageTimeCellList
	}
}

enum class BenchmarkTimeUnit(val shorthand: String) {
	NANOSECONDS("ns"), MICROSECONDS("us"), MILLISECONDS("ms"), SECONDS("s"), MINUTES("m")
}
