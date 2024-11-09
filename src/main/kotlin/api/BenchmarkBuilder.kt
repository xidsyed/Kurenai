package api

import core.Benchmark

class BenchmarkBuilder {
	var setupAction: (() -> Unit)?
		private set
	var tearDownAction: (() -> Unit)?
		private set
	var benchmarkAction: (Benchmark.() -> Unit)?
		private set
	var config: BenchmarkConfig = BenchmarkConfig()
		private set

	init {
		setupAction = null
		tearDownAction = null
		benchmarkAction = null
	}

	/**
	 * @see [BenchmarkConfig] for available configuration options
	 * @param configAction: A lambda that takes a [BenchmarkConfigBuilder] as a receiver and returns Unit.
	 **/
	fun config(configAction: BenchmarkConfigBuilder.() -> Unit) {
		val config = BenchmarkConfigBuilder().apply { configAction() }.build()
		this.config = config
	}

	fun setup(action: () -> Unit) {
		setupAction = action
	}


	fun tearDown(action: () -> Unit) {
		tearDownAction = action
	}

	/**
	 * ### Usage:
	 * ```
	 * 	val benchmark = build("SORT ARRAY BENCHMARK") {
	 * 		bench {
	 * 			config {
	 * 				warmupReps = 5
	 * 				repCount = 100
	 * 				logging = true
	 * 			}
	 *
	 * 			var arr: IntArray? = null
	 * 			var sorted: IntArray? = null
	 *
	 * 			time("Generate 1000 numbers") {
	 * 				arr = IntArray(1000).apply {
	 * 					for (i in indices) set(i, (0..1000).random())
	 * 				}
	 * 			}
	 *
	 * 			time("Get Sorted Array") {
	 * 				sorted = arr!!.sortedArray()
	 * 			}
	 *
	 * 		}
	 * 	}
	 *
	 * 	val result = benchmark.execute(overrideConfig = BenchmarkConfig(..))
	 * ```
	 * This function accepts a `action` lambda that is  used to define a repeatable benchmark action.
	 * - Allows nested benchmarking of code with the [Benchmark.time] function.
	 * - Allows user to define a default [BenchmarkConfig] by passing a lambda to the [BenchmarkBuilder.config] function.
	 * - It also provides an outer-scope for all the internal nested benchmarks to share references to variables and resources.
	 * - Repeated calls to this function replace the existing benchmark action with the new one.
	 **
	 * */
	fun bench(action: Benchmark.() -> Unit) {
		benchmarkAction = action
	}
}


fun build(name: String, buildAction: BenchmarkBuilder.() -> Unit): Benchmark {
	val builder = BenchmarkBuilder()
	builder.buildAction()
	val benchmark = Benchmark(
		name = name,
		setupAction = builder.setupAction,
		tearDownAction = builder.tearDownAction,
		benchAction = builder.benchmarkAction,
		config = builder.config
	)
	return benchmark

}

