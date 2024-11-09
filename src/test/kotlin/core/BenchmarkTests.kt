package core

import api.build
import api.treecell.TreeCell
import api.treecell.data.TimeCell
import com.tschuchort.compiletesting.*
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BenchmarkTests {

	val complexBenchmark = build("COMPLEX") {
		bench {
			time("A1") {
				time("B1") { }
				time("B2") { }
				time("B3") {
					time("C1") {
						time("D1") { }
						time("D2") {
							time("E1") { }
						}
					}
				}
				time("B4") {
					time("C2") { }
					time("C3") { }
				}
				time("B5") { }
			}
			time("A2") { }
		}
	}
	val repeated = build("MINIMAL") {
		bench {
			time("A") { }
		}
		bench {
			time("B") { }
		}
	}
	val singleBenchmark = build("SINGLE") {
		bench {
			time("A") { }
		}
	}
	val emptyBenchmark = build("EMPTY") { }

	private val resultList = listOf(
		complexBenchmark.execute().iterations.first(),
		repeated.execute().iterations.first(),
		singleBenchmark.execute().iterations.first(),
		emptyBenchmark.execute().iterations.first()
	)


	private fun Map<String, TimeCell>.stringify() =
		this.map { (k, v) -> "${k} : ${v.children.joinToString(", ") { it.title }}" }.joinToString("\n")

	private fun List<TimeCell>.stringify() = this.joinToString("\n") { it.title }

	@Test
	fun toMap() {
		val string = resultList.fold(StringBuilder()) { sb, timeCell ->
			val timeCellMap = timeCell.toMap()
			val str = timeCellMap.stringify()
			sb.append(str).appendLine()
		}.trimEnd()
		val expected = """
			COMPLEX : A1, A2
			A1 : B1, B2, B3, B4, B5
			B3 : C1
			C1 : D1, D2
			D2 : E1
			B4 : C2, C3
			MINIMAL : B
			SINGLE : A
		""".trimIndent()
		assertEquals(expected, string)
	}

	@Test
	fun `toMap untrimmed`() {
		val string = resultList.fold(StringBuilder()) { sb, timeCell ->
			val timeCellMap = timeCell.toMap(unTrimmed = true)
			val str = timeCellMap.stringify()
			sb.append(str).appendLine()
		}.trimEnd()
		val expected = """
			COMPLEX : A1, A2
			A1 : B1, B2, B3, B4, B5
			B1 : 
			B2 : 
			B3 : C1
			C1 : D1, D2
			D1 : 
			D2 : E1
			E1 : 
			B4 : C2, C3
			C2 : 
			C3 : 
			B5 : 
			A2 : 
			MINIMAL : B
			B : 
			SINGLE : A
			A : 
			EMPTY :
		""".trimIndent()
		assertEquals(expected, string)
	}

	@Test
	fun `flatten preorder`() {
		val string = resultList.fold(StringBuilder()) { sb, timeCell ->
			val timeCellList = timeCell.flatten()
			sb.append(timeCellList.stringify()).appendLine()
		}.trimEnd()
		println(string)
		val expected = """
			COMPLEX
			A1
			B1
			B2
			B3
			C1
			D1
			D2
			E1
			B4
			C2
			C3
			B5
			A2
			MINIMAL
			B
			SINGLE
			A
			EMPTY
		""".trimIndent()
		assertEquals(expected, string)
	}

	@Test
	fun `flatten postorder`() {
		val string = resultList.fold(StringBuilder()) { sb, el ->
			sb.append(el.flatten(TreeCell.TraversalType.POSTORDER).stringify() + "\n")
		}.trimEnd()

		val expected = """
			B1
			B2
			D1
			E1
			D2
			C1
			B3
			C2
			C3
			B4
			B5
			A1
			A2
			COMPLEX
			B
			MINIMAL
			A
			SINGLE
			EMPTY
		""".trimIndent()

		println(string)
		assertEquals(expected, string)
	}

	@OptIn(ExperimentalCompilerApi::class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class TimeSum {
		val b1 = build("B1") {
			bench {
				timeSum("A1") {
					for (i in 0 until 1000) {
						of { }
					}
				}

				time("A2") {
					timeSum("B1") {
						for (i in 0 until 1000) {
							of { }
						}
					}
				}
			}
		}

		@Nested
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		inner class Compilation {
			@Test
			fun `time inside of timeSum{} does not compile`() {
				val result = compilationErrorTest(
					fileContent = """
						import build
						import execute
						fun main() {
							build("B1") {
								bench {
									timeSum("A1") {
										time("B1") {	
										}
									}
								}
							}
						}
					""".trimIndent(),
				)
				assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
				assert(
					result.messages.contains(
						"fun time(title: String, nodeAction: () -> Unit): Unit' can't be called in this " +
								"context by implicit receiver. Use the explicit one if necessary"
					)
				)
			}

			private fun compilationErrorTest(fileContent: String): KotlinCompilation.Result {
				val source = SourceFile.kotlin("source.kt", fileContent)
				val result = KotlinCompilation().apply {
					sources = listOf(source)
					inheritClassPath = true
					messageOutputStream = System.out
				}.compile()

				return result
			}

		}

	}
}
