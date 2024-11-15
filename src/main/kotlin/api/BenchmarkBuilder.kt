package api

import core.Benchmark
import kotlin.random.Random

/**
 * Builder class for creating a benchmark.
 * Allows configuration of setup, teardown, and benchmark actions.
 * @param setupAction
 * @param tearDownAction
 * @param benchmarkAction
 * @param config
 */
class BenchmarkBuilder {
	private var setupAction: (() -> Unit)?
	private var tearDownAction: (() -> Unit)?
	private var benchmarkAction: (Benchmark.() -> Unit)?
	private var config: BenchmarkConfig = BenchmarkConfig()

	init {
		setupAction = null
		tearDownAction = null
		benchmarkAction = null
	}

	/**
	 * @see [BenchmarkConfig] for available configuration options
	 * @param configAction A lambda that builds a [BenchmarkConfig] and returns Unit.
	 * ```
	 * config {
	 * 	iterations = 1
	 * 	logging = false
	 * 	warmupIterations = 0
	 * 	formattedTable = false
	 * }
	 * ```
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
	 * This function accepts a `action` lambda that is  used to define a repeatable benchmark action.
	 * - Allows nested benchmarking of code with the [Benchmark.time] function.
	 * - Allows user to define a default [BenchmarkConfig] by passing a lambda to the [BenchmarkBuilder.config] function.
	 * - It also provides an outer-scope for all the internal nested benchmarks to share references to variables and resources.
	 * - Repeated calls to this function replace the existing benchmark action with the new one.
	 * ### Example:
	 * ```
	 * 	val benchmark = build("SORT ARRAY BENCHMARK") {
	 * 		bench {
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
	 * 		}
	 * 	}
	 *
	 * 	val result = benchmark.execute(overrideConfig = BenchmarkConfig(..))
	 * ```

	 * */
	fun bench(action: Benchmark.() -> Unit) {
		benchmarkAction = action
	}

	internal fun toBenchmark(name: String) = Benchmark(
		name = name,
		setupAction = setupAction,
		tearDownAction = tearDownAction,
		benchAction = benchmarkAction,
		config = config
	)
}

/**
 * Builds a benchmark with the specified name and configuration.
 * @param name The name of the benchmark.
 * @param buildAction A lambda that defines the benchmark configuration using the [BenchmarkBuilder].
 * @return A Benchmark instance configured with the specified settings.
 *
 * Here is a simple example:
 * ```
 * 	build("benchmark") {
 * 		config {
 * 			warmupIterations = 3
 * 			iterations = 10
 * 		}
 *
 * 		lateinit var list: MutableList<Int>
 * 		setup { buildList { repeat(1000) { add(Random.nextInt()) } } }
 * 		bench {
 * 			time("increment list") {
 * 				for (i in 0..list.size) {
 * 					list[i] = list[i] + 1
 * 				}
 * 			}
 * 			time("filter and sort") {
 * 				time("filter even elements") {
 * 					for (i in 0..list.size) {
 * 						list[i] = if (list[i] % 2 == 0) 0 else list[i]
 * 					}
 * 				}
 * 				time("sort list") { list.sorted() }
 * 			}
 * 		}
 * 		tearDown { list.replaceAll { -1 } }
 * 	}
 *
 * ```
 */

fun build(name: String, buildAction: BenchmarkBuilder.() -> Unit): Benchmark {
	val builder = BenchmarkBuilder()
	builder.buildAction()
	return builder.toBenchmark(name)
}

fun test() {
	build("benchmark") {
		config {
			warmupIterations = 3
			iterations = 10
		}

		lateinit var list: MutableList<Int>
		setup { buildList { repeat(1000) { add(Random.nextInt()) } } }
		bench {
			time("increment list") {
				for (i in 0..list.size) {
					list[i] = list[i] + 1
				}
			}
			time("filter and sort") {
				time("filter even elements") {
					for (i in 0..list.size) {
						list[i] = if (list[i] % 2 == 0) 0 else list[i]
					}
				}
				time("sort list") { list.sorted() }
			}
		}
		tearDown { list.replaceAll { -1 } }
	}
}

