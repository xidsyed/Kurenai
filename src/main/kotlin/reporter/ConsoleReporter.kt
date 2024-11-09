package reporter

import api.BenchmarkResult
import api.treecell.TreeCellState
import core.parser.Parser
import de.m3y.kformat.*

class ConsoleReporter(private val parser: Parser) {

	fun printSummary() {
		println(parser.summary)
	}

	fun printOverallResults() {
		println(table {
			header("Iteration", "Total Time (ns)")

			parser.benchTimes().forEachIndexed { index, timeCell ->
				row(index + 1, (timeCell.timeState as TreeCellState.Complete).duration)
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Iteration", Table.Hints.Alignment.RIGHT)
				alignment("Total Time (ns)", Table.Hints.Alignment.RIGHT)
			}
		}.render())
	}

	fun printDetailedResults(iteration: Int = 0) {
		println(table {
			header("Operation", "Duration (ns)")

			parser.iterationList(iteration).forEach { timeCell ->
				row(timeCell.title, (timeCell.timeState as TreeCellState.Complete).duration)
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Operation", Table.Hints.Alignment.LEFT)
				alignment("Duration (ns)", Table.Hints.Alignment.RIGHT)
			}
		}.render())
	}

	fun printAverageResults() {
		val averageIteration = parser.averageIteration()
		println(table {
			header("Operation", "Average Duration (ns)")

			averageIteration.flatten().forEach { timeCell ->
				row(timeCell.title, (timeCell.timeState as TreeCellState.Complete).duration)
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Operation", Table.Hints.Alignment.LEFT)
				alignment("Average Duration (ns)", Table.Hints.Alignment.RIGHT)
			}
		}.render())
	}

	fun printComparisonTable() {
		val operations = parser.iterationList(0).map { it.title }
		println(table {
			header("Operation", *List(parser.benchTimes().size) { "Iter ${it + 1}" }.toTypedArray(), "Average")

			operations.forEach { operation ->
				val row = mutableListOf<Any>(operation)
				row.addAll(parser.cellTimes(operation).map { it?.duration ?: "N/A" })
				row.add(parser.averageIteration().flatten().find { it.title == operation }?.duration ?: "N/A")
				row(*row.toTypedArray())
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Operation", Table.Hints.Alignment.LEFT)
				(1..parser.benchTimes().size + 1).forEach {
					alignment(it, Table.Hints.Alignment.RIGHT)
				}
			}
		}.render())
	}

	fun printHierarchicalResults(iteration: Int = 0) {
		fun printNode(node: api.treecell.data.TimeCell, depth: Int = 0) {
			val indent = "  ".repeat(depth)
			println("$indent${node.title}: ${(node.timeState as TreeCellState.Complete).duration} ns")
			node.children.forEach { printNode(it, depth + 1) }
		}

		printNode(parser.benchTime(iteration))
	}

	companion object {
		fun fromBenchmarkResult(result: BenchmarkResult): ConsoleReporter {
			return ConsoleReporter(Parser(result))
		}
	}
}