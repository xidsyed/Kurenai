package reporter.utils

object FormattedTime {

	fun format(value: Long, unit: UnitOfTime): Long = value / unit.inNanos

	fun formatString(value: Long, unit: UnitOfTime) = "${value / unit.inNanos}${unit.suffix}"

/*	fun formattedString(value : Long)  :String {
		listOf(Day)
	}*/
}

enum class UnitOfTime(val inNanos: Long, val suffix: String) {
	DAY(86_400_000_000_000, "d"),
	HOUR(3_600_000_000_000, "h"),
	MINUTE(60_000_000_000, "m"),
	SECOND(1_000_000_000, "s"),
	MILLISECOND(1_000_000, "ms"),
	MICROSECOND(1_000, "μs");
}


class FormattedState(val value: Long, val sb: StringBuilder)

//fun TimeCell.format(): FormattedTime? = this.duration?.let { reporter.utils.FormattedTime(it) }

