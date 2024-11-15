package reporter

import api.*
import core.execute
import core.parser.Parser
import org.junit.jupiter.api.*
import reporter.utils.UnitOfTime
import trimAllIndent
import kotlin.random.Random
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TableReporterTest {

	private lateinit var benchmarkResult: BenchmarkResult
	private lateinit var parser: Parser

	@BeforeAll
	fun setup() {
		benchmarkResult = build("COMPLEX") {
			config {
				warmupIterations = 2
				iterations = 3
			}

			bench {
				time("A1") {
					time("B1") { Thread.sleep(10) }
					time("B2") { Thread.sleep(20) }
					time("B3") {
						time("C1") {
							time("D1") { Thread.sleep(5) }
							time("D2") {
								time("E1") { Thread.sleep(15) }
							}
						}
					}
					time("B4") {
						time("C2") { Thread.sleep(10) }
						time("C3") { Thread.sleep(5) }
					}
					time("B5") { Thread.sleep(5) }
				}
				time("A2") { Thread.sleep(30) }
			}
		}.execute()

		parser = Parser(benchmarkResult)
	}

	@Test
	fun printTable() {
		val reporter = TableReporter(parser)
		reporter.setReportingUnit(UnitOfTime.MILLISECOND)
		println(
			"""
				SUMMARY
				${reporter.summary()}
				
				AVERAGE ITERATION
				${reporter.averageIteration()}
				
				ALL ITERATIONS
				${reporter.allIterations()}
				
				ITERATION TIMES
				${reporter.iterationTimes()}
				
				HIERARCHICAL RESULTS
				${reporter.hierarchicalResults()}
				
				COMPARISON TABLE
				${reporter.comparisonTable()}			
			""".trimAllIndent()

		)
	}

	@Test
	fun test() {
		val benchmark = build("benchmark") {
			config {
				warmupIterations = 3
				iterations = 10
			}

			lateinit var list: MutableList<Int>
			setup { list = MutableList(100) { Random.nextInt() } }
			bench {
				time("increment list") {
					for (i in 0 until list.size) {
						list[i]++
					}
				}
				time("filter and sort") {
					time("filter even elements") {
						for (i in 0 until list.size) {
							list[i] = if (list[i] % 2 == 0) 0 else list[i]
						}
					}
					time("sort list") { list.sorted() }
				}
			}
			tearDown { list.replaceAll { -1 } }
		}

		val reporter: TableReporter = Reporter.fromBenchmark(benchmark)
		reporter.setReportingUnit(UnitOfTime.MILLISECOND)
		println(
			"""
			SUMMARY
			${reporter.summary()}
			AVERAGE ITERATION
			${reporter.averageIteration()}
			HIERARCHICAL RESULTS
			${reporter.hierarchicalResults()}
			""".trimAllIndent()
		)
	}


}