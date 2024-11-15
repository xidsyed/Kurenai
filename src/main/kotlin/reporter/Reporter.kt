package reporter

import api.BenchmarkResult
import core.*
import core.parser.Parser
import reporter.utils.UnitOfTime

abstract class Reporter(protected open val parser: Parser) {
	protected var unit: UnitOfTime = UnitOfTime.NANOSECOND
	fun setReportingUnit(unit: UnitOfTime) {
		this.unit = unit
	}

	companion object {
		inline fun <reified T : Reporter> fromBenchmarkResults (result : BenchmarkResult): T {
			return T::class.java.getDeclaredConstructor(Parser::class.java).newInstance(Parser(result))
		}

		inline fun <reified T : Reporter> fromBenchmark (benchmark: Benchmark): T {
			val result = benchmark.execute()
			return fromBenchmarkResults(result)
		}
	}
}

