package core.parser

import api.BenchmarkResult
import api.treecell.TreeCell.TraversalType.PREORDER
import api.treecell.TreeCellState.Complete
import api.treecell.data.*
import reporter.utils.UnitOfTime
import trimAllIndent

/**
 * A parser for benchmarking results.
 *
 * @property result The benchmark result to be parsed.
 */
class Parser(result: BenchmarkResult) {

	val benchmarkName: String = result.title
	val nanoseconds: Long = result.benchmarkTime
	val iterationCount = result.iterations.size
	val warmupIterationCount = result.warmupIterations.size

	private val iterHolders: List<IterationHolder> = result.iterations.map { IterationHolder(it) }
	private val warmupIterHolders: List<IterationHolder> = result.warmupIterations.map { IterationHolder(it) }

	/**
	 * A summary of the benchmark result.
	 */
	val summary: String = """
        Benchmark Name				: $benchmarkName
        Benchmark Time 				: ${nanoseconds.div(UnitOfTime.MILLISECOND.inNanos)}ms | ${nanoseconds}ns
        Warmup Iterations			: $warmupIterationCount
        Benchmark Iterations		: $iterationCount
    """.trimAllIndent()

	/**
	 * Retrieves the benchmark time for a specific iteration.
	 *
	 * @param iterationIndex The index of the iteration.
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return The root time cell of the specified iteration.
	 */
	fun rootCellFromIteration(
		iterationIndex: Int,
		iterationType: IterationType = IterationType.BENCHMARK_ITERATION
	): TimeCell {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		return holders[iterationIndex].rootCell
	}

	/**
	 * Retrieves the benchmark times for all iterations.
	 *
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return A list of root time cells for all iterations.
	 */
	fun rootCellsFromAllIterations(iterationType: IterationType = IterationType.BENCHMARK_ITERATION): List<TimeCell> {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		return holders.map { it.rootCell }
	}

	/**
	 * Retrieves a list of time cells for a specific iteration, ordered by the specified order.
	 *
	 * @param index The index of the iteration.
	 * @param order The order in which to list the iterations (call order or completion order).
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return A list of time cells for the specified iteration in the specified order.
	 */
	fun getIterationTimeCellsAsList(
		index: Int,
		order: IterationListOrder = IterationListOrder.CALL_ORDER,
		iterationType: IterationType = IterationType.BENCHMARK_ITERATION
	): List<TimeCell> {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		return if (order == IterationListOrder.COMPLETION_ORDER) holders[index].completionOrderedList
		else holders[index].callOrderedList
	}


	/**
	 * Retrieves a specific time cell by title for a given iteration.
	 *
	 * @param title The title of the time cell to retrieve.
	 * @param iteration The index of the iteration.
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return The time cell associated with the title in the specified iteration, or null if not found.
	 */
	fun timeCellByTitleFromIteration(
		title: String,
		iteration: Int,
		iterationType: IterationType = IterationType.BENCHMARK_ITERATION
	): TimeCell? {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		return holders[iteration].titleMap[title]
	}

	/**
	 * Retrieves a list of time cells by title across all iterations.
	 *
	 * @param title The title of the time cells to retrieve.
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return A list of time cells associated with the title across all iterations, with null for missing titles.
	 */
	fun timeCellListByTitle(
		title: String,
		iterationType: IterationType = IterationType.BENCHMARK_ITERATION
	): List<TimeCell?> {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		return holders.map { it.titleMap[title] }
	}

	/**
	 * Calculates the average duration of a list of time cells.
	 *
	 * @param list The list of time cells to average.
	 * @return The average duration as a Long.
	 */
	private fun averageCell(list: List<TimeCell>): Long {
		var sum = 0L
		var count = 0
		list.forEach { cell -> cell.getDuration()?.let { sum += it; count++ } }
		return sum / count
	}

	/**
	 * Computes the average benchmark time across all iterations.
	 *
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return A TimeCell representing the average benchmark time.
	 */
	fun averageBenchmarkTime(iterationType: IterationType = IterationType.BENCHMARK_ITERATION): TimeCell {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		return TimeCell(
			holders.first().rootCell.title,
			Complete(averageCell(holders.map { it.rootCell }))
		)
	}

	/**
	 * Generates an iteration that is the average of all the iterations of the specified [IterationType]
	 *
	 * @param iterationType The type of iteration (BENCHMARK_ITERATION or WARMUP_ITERATION).
	 * @return A root [TimeCell] representing the average of all time cells across iterations.
	 */
	fun generateAveragedIteration(iterationType: IterationType = IterationType.BENCHMARK_ITERATION): TimeCell {
		val holders = if (iterationType == IterationType.BENCHMARK_ITERATION) iterHolders else warmupIterHolders
		val mTree = holders.first().rootCell.toMutableTimeCell()    // First Iteration Source of truth
		val mList = mTree.flatten(traversalType = PREORDER)
		val sumCount = IntArray(mList.size) { 1 }
		mList.forEachIndexed { mIndex, mCell ->
			for (i in 1 until holders.size) {
				val holder = holders[i]
				val iCell = holder.titleMap[mCell.title]
				iCell?.let {
					if (mCell._timeState is Complete && it.timeState is Complete) sumCount[mIndex]++
					mCell.addTimeState(it.timeState)
				}
			}
			if (mCell._timeState is Complete) {
				val _state = mCell._timeState as Complete
				val sumDuration = _state.duration
				_state.forceSetDuration((sumDuration / sumCount[mIndex]))
			}
		}
		return mTree.toTimeCell()
	}

	enum class IterationType {
		BENCHMARK_ITERATION,
		WARMUP_ITERATION
	}
}

