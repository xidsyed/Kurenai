package reporter

import api.*
import api.treecell.TreeCellState
import core.execute
import core.parser.*
import org.junit.jupiter.api.*
import kotlin.test.*
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTest {

	private lateinit var benchmarkResult: BenchmarkResult
	private lateinit var parser: Parser

	@BeforeAll
	fun setup() {
		benchmarkResult = build("COMPLEX") {
			config {
				warmupReps = 2
				repCount = 3
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
	fun testSummary() {
		val expectedSummary = """
            Benchmark Name: COMPLEX
            Benchmark Completion Time: ${benchmarkResult.benchmarkTime}ms
            Warmup Iterations: 2
            Benchmark Iterations: 3
        """.trimIndent()
		assertEquals(expectedSummary, parser.summary)

	}

	@Test
	fun testBenchTime() {
		val expectedResults = mutableListOf<String>()
		val actualResults = mutableListOf<String>()
		for(i in 0  until benchmarkResult.iterations.size) {
			expectedResults.add(benchmarkResult.iterations[i].toString())
			actualResults.add(parser.benchTime(i).toString())
		}
		assertEquals(expectedResults.toString(), actualResults.toString())
	}

	@Test
	fun testBenchTimes() {
		val benchTimes = parser.benchTimes()
		assertEquals(3, benchTimes.size)
		benchTimes.forEach {
			assertEquals("COMPLEX", it.title)
			assert(it.timeState is TreeCellState.Complete)
		}
	}

	@Test
	fun testIterationList() {
		val callOrderList = parser.iterationList(0, IterationListOrder.CALL_ORDER)
		val expectedCallOrder =
			listOf("COMPLEX", "A1", "B1", "B2", "B3", "C1", "D1", "D2", "E1", "B4", "C2", "C3", "B5", "A2")
		assertEquals(expectedCallOrder, callOrderList.map { it.title })

		val completionOrderList = parser.iterationList(0, IterationListOrder.COMPLETION_ORDER)
		val expectedCompletionOrder =
			listOf("B1", "B2", "D1", "E1", "D2", "C1", "B3", "C2", "C3", "B4", "B5", "A1", "A2", "COMPLEX")
		assertEquals(expectedCompletionOrder, completionOrderList.map { it.title })
	}

	@Test
	fun testCellTime() {
		val a1Cell = parser.cellTime("A1", 0)
		assertNotNull(a1Cell)
		assertEquals("A1", a1Cell.title)
		assert(a1Cell.timeState is TreeCellState.Complete)

		val nonExistentCell = parser.cellTime("NonExistent", 0)
		assertEquals(null, nonExistentCell)
	}

	@Test
	fun testCellTimes() {
		val a1Cells = parser.cellTimes("A1")
		assertEquals(3, a1Cells.size)
		a1Cells.forEach {
			assertNotNull(it)
			assertEquals("A1", it.title)
			assert(it.timeState is TreeCellState.Complete)
		}

		val nonExistentCells = parser.cellTimes("NonExistent")
		assertEquals(3, nonExistentCells.size)
		nonExistentCells.forEach { assertEquals(null, it) }
	}

	@Test
	fun testAverageBenchTime() {
		val averageBenchTime = parser.averageBenchTime()
		assertEquals("COMPLEX", averageBenchTime.title)
		assert(averageBenchTime.timeState is TreeCellState.Complete)

		val actualAverageTime =
			benchmarkResult.iterations.map { (it.timeState as TreeCellState.Complete).duration }.average().toLong()
		assertEquals(actualAverageTime, (averageBenchTime.timeState as TreeCellState.Complete).duration)
	}

	@Test
	fun testAverageIteration() {
		val averageIteration = parser.averageIteration()
		val actualList = averageIteration.flatten()
		assertEquals("COMPLEX", averageIteration.title)
		assert(averageIteration.timeState is TreeCellState.Complete)

		// Check if all expected nodes are present
		val expectedTitles =
			listOf("COMPLEX", "A1", "B1", "B2", "B3", "C1", "D1", "D2", "E1", "B4", "C2", "C3", "B5", "A2")
		val actualTitles = actualList.map { it.title }
		assertEquals(expectedTitles.toSet(), actualTitles.toSet())

		// Check if average times are calculated correctly
		expectedTitles.forEach { title ->
			var count = 0
			val durationList = benchmarkResult.iterations
				.mapNotNull { iter ->
					val iterList = iter.flatten()
					val cell = iterList.find { cell -> cell.title == title }
					return@mapNotNull cell?.duration?.let{
						count++
						cell.duration
					}
				}
			val expectedAvgTime = if(durationList.isNotEmpty()) (durationList.sum() / count) else null
			val actualAvgTime = actualList.find { it.title == title }?.duration
			assertEquals(
				expectedAvgTime,
				actualAvgTime,
				message = "$title\nExpected:$expectedAvgTime\nActual:$actualAvgTime\n"
			)
		}
	}

	@Test
	fun main() {
		val reporter = ConsoleReporter(parser)
		reporter.printSummary()
		reporter.printAverageResults()
		reporter.printOverallResults()
		reporter.printDetailedResults()
		reporter.printHierarchicalResults()
		reporter.printComparisonTable()
	}
}