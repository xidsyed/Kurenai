package reporter

import api.treecell.data.TimeCell
import core.parser.Parser
import core.parser.Parser.IterationType
import core.parser.Parser.IterationType.BENCHMARK_ITERATION
import de.m3y.kformat.*

class TableReporter(parser: Parser) : Reporter(parser) {

	fun summary(): String = parser.summary

	fun allIterations(it: IterationType = BENCHMARK_ITERATION): String {
		return table {
			header("Iteration", "Total Time (${unit.suffix})")

			parser.rootCellsFromAllIterations(it).forEachIndexed { index, timeCell ->
				row(index + 1, getDuration(timeCell))
			}
			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Iteration", Table.Hints.Alignment.RIGHT)
				alignment("Total Time (${unit.suffix})", Table.Hints.Alignment.RIGHT)
			}
		}.render().toString()
	}

	fun iterationTimes(iteration: Int = 0, it: IterationType = BENCHMARK_ITERATION): String {
		return table {
			header("Operation", "Duration (${unit.suffix})")

			parser.getIterationTimeCellsAsList(iteration, iterationType = it).forEach { timeCell ->
				row(timeCell.title, getDuration(timeCell))
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Operation", Table.Hints.Alignment.LEFT)
				alignment("Duration (${unit.suffix})", Table.Hints.Alignment.RIGHT)
			}
		}.render().toString()
	}

	fun averageIteration(it: IterationType = BENCHMARK_ITERATION): String {
		val averageIteration = parser.generateAveragedIteration(it)
		return table {
			header("Operation", "Average Duration (${unit.suffix})")

			averageIteration.flatten().forEach { timeCell ->
				row(timeCell.title, getDuration(timeCell))
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Operation", Table.Hints.Alignment.LEFT)
				alignment("Average Duration (${unit.suffix})", Table.Hints.Alignment.RIGHT)
			}
		}.render().toString()
	}

	fun comparisonTable(it: IterationType = BENCHMARK_ITERATION): String {
		val operations = parser.getIterationTimeCellsAsList(0, iterationType = it).map { it.title }
		val table = table {
			header(
				"Operation",
				*List(parser.rootCellsFromAllIterations(iterationType = it).size) { "Iter ${it + 1} (${unit.suffix})" }.toTypedArray(),
				"Average"
			)

			operations.forEach { operation ->
				val row = mutableListOf<Any>(operation)
				row.addAll(parser.timeCellListByTitle(operation, it).map { timeCell ->
					timeCell?.let {
						getDuration(it)
					} ?: "N/A"
				})
				row.add(
					parser
						.generateAveragedIteration(it)
						.flatten()
						.find { it.title == operation }
						?.let { getDuration(it) }
						?: "N/A"
				)
				row(*row.toTypedArray())
			}

			hints {
				borderStyle = Table.BorderStyle.SINGLE_LINE
				alignment("Operation", Table.Hints.Alignment.LEFT)
				(1..parser.rootCellsFromAllIterations(it).size + 1).forEach {
					alignment(it, Table.Hints.Alignment.RIGHT)
				}
			}
		}
		return table.render().toString()
	}

	fun hierarchicalResults(iteration: Int = 0, it: IterationType = BENCHMARK_ITERATION): String {
		val sb = StringBuilder()
		appendNode(sb, parser.rootCellFromIteration(iteration, it))
		return sb.toString()
	}

	private fun appendNode(sb: StringBuilder, node: TimeCell, depth: Int = 0) {
		val indent = "  ".repeat(depth)
		sb.append("$indent${node.title}: ${getDuration(node)}${unit.suffix}\n")
		node.children.forEach { appendNode(sb, it, depth + 1) }
	}

	private fun getDuration(timeCell: TimeCell): String {
		return (timeCell.getDuration(unit))?.toString() ?: timeCell.timeState.toString()
	}


}