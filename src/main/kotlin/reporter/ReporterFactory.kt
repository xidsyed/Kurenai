package reporter

import api.BenchmarkResult
import core.*

interface ReporterFactory<T: Reporter> {
	fun fromBenchmark(benchmark: Benchmark) : T{
		return fromBenchmarkResults(benchmark.execute())
	}
	fun fromBenchmarkResults(result: BenchmarkResult) : T
}