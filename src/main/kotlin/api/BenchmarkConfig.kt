package api


/**
 * A data class that holds the configuration for a benchmark.
 * @param iterations: The number of times the benchmark should be run
 * @param logging: A boolean flag to enable or disable logging. Enabling will print a line to the console for each iteration
 * @param warmupIterations: The number of warmup iterations to run before the actual benchmark. Default is 0
 * @param formattedTable: A boolean flag to enable or disable formatted table output. Enabling will print a formatted table of the results
 * */
data class BenchmarkConfig(
	val name: String = "",
	val iterations: Int = 10,
	val logging: Boolean = false,
	val warmupIterations: Int = 0,
	val formattedTable: Boolean = false
) {
	init {
		require(iterations > 0) { "'iterations' must be greater than 0, but was $iterations." }
		require(warmupIterations >= 0) { "'warmupIterations' cannot be negative, but was $warmupIterations." }
	}
}