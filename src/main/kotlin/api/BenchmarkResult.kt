package api

import api.treecell.data.TimeCell

/**
 * Container for the results of a benchmark
 * @param title: The title of the benchmark
 * @param config: The configuration used for the benchmark
 * @param iterations: The results of the benchmark is a list of [TimeCell]s which may each have children
 * @param warmupIterations: The results of the warmup runs
 * @param benchmarkTime: The total time taken to run the benchmark
 * */
class BenchmarkResult(
	val title: String,
	val config: BenchmarkConfig,
	val iterations: List<TimeCell>,
	val warmupIterations: List<TimeCell>,
	val benchmarkTime: Long,
)

