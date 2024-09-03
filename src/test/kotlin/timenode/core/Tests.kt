package timenode.core

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import api.build
import api.timecell.TimeCell
import api.timecell.TraversalType.*
import api.timecell.flatten
import api.timecell.toMap
import core.execute

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Tests {

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


	private fun Map<TimeCell, List<TimeCell>>.stringify() =
		this.map { (k, v) -> "${k.title} : ${v.joinToString(", ") { it.title }}" }.joinToString("\n")

	private fun List<TimeCell>.stringify() = this.joinToString("\n") { it.title }

	@Test
	fun toMap() {
		val string = resultList.fold(StringBuilder()) { sb, el ->
			sb.append(el.toMap().stringify() + "\n")
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
		val string = resultList.fold(StringBuilder()) { sb, el ->
			sb.append(el.toMap(unTrimmed = true).stringify() + "\n")
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
		val string = resultList.fold(StringBuilder()) { sb, el ->
			sb.append(el.flatten().stringify() + "\n")
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
			sb.append(el.flatten(POSTORDER).stringify() + "\n")
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

		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		class Compilation {
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
