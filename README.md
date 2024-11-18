<h1 align="center"><b>Kurunei: A Kotlin Benchmark DSL</b></h1>

<p align="center"><a href="https://acqcc.blog/#kurunai---a-benchmarking-dsl-"><img src="https://i.imgur.com/ashCst2.png" width="200"></a></p> 


A powerful, flexible benchmarking library for Kotlin that enables you to create configurable benchmarks with nested time measurements, warmup iterations, and detailed reporting capabilities.

## Features

- 🔧 **Configurable Benchmarks**: Set warmup iterations and benchmark iterations
- 🏗️ **Setup & Teardown Support**: Define initialization and cleanup code for your benchmarks
- ⏱️ **Nested Time Measurements**: Create hierarchical time measurements for detailed profiling
- 📊 **Comprehensive Reporting**: Generate detailed reports including averages, hierarchical results, and comparison tables
- ⚡ **Time Block Aggregation**: Aggregate time measurements for repeated operations
- 🎯 **Conditional Timing**: Measure time only when specific conditions are met

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    ...
    implementation("com.github.xidsyed:Kurenai:0.1.0")
}

```

## Quick Start

Here's a simple example that demonstrates the main features of the library:

```kotlin
val benchmark = build("benchmark") {
    config {
        warmupIterations = 3
        iterations = 10
    }

    lateinit var list: MutableList<Int>
    
    setup { 
        list = MutableList(1000) { Random.nextInt() } 
    }
    
    bench {
        time("increment list") {
            for (i in 0 until list.size) {
                list[i]++
            }
        }
        
        time("filter and sort") {
            timeSum("filter even elements") {
                for (i in 0 until list.size) {
                    of {
                        list[i] = if (list[i] % 2 == 0) 0 else list[i]
                    }
                }
            }
            time("sort list") { list.sorted() }
        }
    }
    
    tearDown { 
        list.replaceAll { -1 } 
    }
}

// Generate and print report
Reporter.fromBenchmark<TextReporter>(benchmark).apply {
    setReportingUnit(UnitOfTime.MILLISECOND)
    println(fullReport())
}
```

## Key Concepts

### Benchmark Configuration

Configure your benchmarks using the `config` block:
```kotlin
config {
    warmupIterations = 3  // Number of warmup iterations
    iterations = 10       // Number of actual benchmark iterations
    logging = true       // Enable/disable logging
}
```

### Time Measurements

The library provides three types of time measurements:

1. **Simple Time Measurement**
```kotlin
time("operation name") {
    // Code to measure
}
```

2. **Aggregated Time Measurement**
```kotlin
timeSum("repeated operation") {
    for (item in items) {
        of {
            // Code to measure repeatedly
        }
    }
}
```

3. **Conditional Time Measurement**
```kotlin
timeIf(condition, "conditional operation") {
    // Code to measure if condition is true
}
```

### Report Types

The library supports multiple report formats the simplest of which is a `TextReport` and is generated and printed like so.

```kotlin
// Generate and print report
Reporter.fromBenchmark<TextReporter>(benchmark).apply {
    setReportingUnit(UnitOfTime.MILLISECOND)
    println(fullReport())
}
```

Here's what's included in the text report:

- Summary statistics
- Average iteration times
- All iterations overview
- Detailed iteration times
- Hierarchical results
- Comparison table across iterations

Keep in mind this library is still in early stages, the final form of this library will include interected html reports that can then be exported into any format.

## Sample Output

```
SUMMARY
Benchmark Name           : benchmark
Benchmark Time          : 30.7524ms | 30752400ns
Warmup Iterations       : 3
Benchmark Iterations    : 10

AVERAGE ITERATION
Operation            | Average Duration (ms)
---------------------|----------------------
benchmark            |                   0.4
increment list       |                   0.1
filter and sort      |                   0.3
filter even elements |                   0.1
sort list            |                   0.2

HIERARCHICAL RESULTS
benchmark: 0.6ms
  increment list: 0.1ms
  filter and sort: 0.5ms
    filter even elements: 0.1ms
    sort list: 0.4ms

<truncated for brevity>
```

## Advanced Usage

### Custom Reporters

You can create custom reporters by implementing the `Reporter` interface:

```kotlin
class CustomReporter : Reporter {
    override fun fullReport(): String {
        // Implement custom reporting logic
    }
}
```

### Benchmark Execution Control

Execute benchmarks with custom configurations:

```kotlin
val result = benchmark.execute(
    overrideConfig = BenchmarkConfig(
        warmupIterations = 5,
        iterations = 20
    )
)
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

Developed with ❤️ by [**Syed**@acqcc.blog](https://acqcc.blog)

## Project Status

This project is actively maintained. Feel free to open issues or submit pull requests.
