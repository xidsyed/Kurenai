package api

/**
 * Builder class for configuring benchmark settings.
 * @param name The name of the benchmark.
 * @param iterations The number of repetitions for the benchmark.
 * @param logging A flag to enable or disable logging.
 * @param warmupIterations The number of warmup repetitions.
 * @param formattedTable A flag to enable or disable formatted table output.
 */
class BenchmarkConfigBuilder {
	var iterations: Int = 1
	var logging: Boolean = false
	var warmupIterations: Int = 0
	var formattedTable: Boolean = false

	fun build(): BenchmarkConfig {
		return BenchmarkConfig(
			iterations = iterations,
			logging = logging,
			warmupIterations = warmupIterations,
			formattedTable = formattedTable
		)
	}
}