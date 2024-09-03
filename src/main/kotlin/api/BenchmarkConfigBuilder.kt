package api

class BenchmarkConfigBuilder {
	var name: String = "Untitled"
	var repCount: Int = 1
	var logging: Boolean = false
	var warmupReps: Int = 0
	var formattedTable: Boolean = false

	fun build(): BenchmarkConfig {
		return BenchmarkConfig(
			name = name,
			iterations = repCount,
			logging = logging,
			warmupIterations = warmupReps,
			formattedTable = formattedTable
		)
	}
}