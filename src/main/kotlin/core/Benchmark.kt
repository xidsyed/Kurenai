package core

import api.*
import api.treecell.data.toTimeCell
import core.timenode.*
import core.timenode.TimeNodeState.Completed.Complete
import core.timenode.TimeNodeState.Completed.Invalid
import nanoToTimeString
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureNanoTime


@DslMarker
annotation class NonNestedDSL

@NonNestedDSL
class Benchmark(
	val name: String,
	private val setupAction: (() -> Unit)? = null,
	private val tearDownAction: (() -> Unit)? = null,
	private val benchAction: (Benchmark.() -> Unit)? = null,
	var config: BenchmarkConfig = BenchmarkConfig()
) {
	private val timeTree: TimeTree = TimeTree(name)

	/**
	 * Use to aggregate time measurements in a single node using [TimeSum.of] method call.
	 * Use it within for loops or other repeating actions to aggregate time measurements.
	 *
	 * Calls to [timeSum] cannot be nested.
	 * */
	fun timeSum(title: String, action: TimeSum.() -> Unit) {
		TimeSum(timeTree).run(title, action)
	}

	/**
	 * Use to measure time of a single action, that is not repeated.
	 * Calls to [time] can be nested and will create nested nodes in the time tre
	 * */
	fun time(title: String, nodeAction: () -> Unit) {
		timeTree.addTimeNode(title) {
			Complete(measureNanoTime { nodeAction() })
		}
	}

	/**
	 * Use to measure time of a single action, that is not repeated.
	 * In case the condition fails, the time measurement will be marked as invalid.
	 *
	 * Calls to [timeIf] can be nested but must be used intentionally
	 * */
	fun timeIf(condition: Boolean, title: String, action: () -> Unit) {
		timeTree.addTimeNode(title) {
			if (condition) Complete(measureNanoTime { action() })
			else Invalid
		}
	}

	fun run(): BenchmarkResult {
		val iterationTimes = arrayListOf<TimeNode>().apply{ensureCapacity(config.iterations)}
		val warmupTimes = arrayListOf<TimeNode>().apply {ensureCapacity(config.warmupIterations)}
		val totalBenchTime = measureNanoTime {
			setupAction?.let {
				it()
				log("Setup complete")
			} ?: log("No Setup action provided")
			if (config.warmupIterations > 0) {
				repeat(config.warmupIterations) { it ->
					val time = measureNanoTime { benchAction?.let { it() } }
					timeTree.iterationRoot.complete(time)
					warmupTimes.add(timeTree.iterationRoot)
					timeTree.resetTree()
					log("Warmup iteration ${it + 1} took ${time.nanoToTimeString()}")
				}
			}

			repeat(config.iterations) {
				var singleBenchTime = 0L
				try {
					singleBenchTime = measureNanoTime { benchAction?.let { it() } }
				} catch (e: Exception) {
					log("Error in iteration ${it + 1}: ${e.message}")
					timeTree.iterationRoot.complete(TimeNodeState.Completed.Error)
				}
				timeTree.iterationRoot.complete(singleBenchTime)
				iterationTimes.add(timeTree.iterationRoot)
				timeTree.resetTree()
				log("Iteration ${it + 1} took ${singleBenchTime.nanoToTimeString()}")
			}

			tearDownAction?.let {
				it()
				log("Tear Down complete")
			} ?: log("No Tear Down action provided")
		}
		log("Benchmark complete in ${totalBenchTime.nanoToTimeString()}")

		log("Creating Benchmark Result")
		return BenchmarkResult(
			title = name,
			config = config,
			iterations = iterationTimes.map {it.toTimeCell()},
			warmupIterations = warmupTimes.map {it.toTimeCell()},
			benchmarkTime = totalBenchTime
		)
	}

	private fun log(message: String) {
		if (config.logging) {
			val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSS"))
			println("$timestamp	$message")
		}
	}
}


fun Benchmark.execute(overrideConfig: BenchmarkConfig? = null): BenchmarkResult {
	overrideConfig?.let { config = it }
	return this.run()
}
