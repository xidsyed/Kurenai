package api.treecell

sealed interface TreeCellState {

	object Invalid : TreeCellState {
		override fun toString(): String {
			return "TreeCellState.Invalid"
		}
	}
	object Error : TreeCellState {
		override fun toString(): String {
			return "TreeCellState.Error"
		}
	}
	class Complete(private var _duration: Long) : TreeCellState{
		val duration get() = _duration
		fun forceSetDuration(value: Long) {
			_duration = value
		}
		override fun toString(): String {
			return "TreeCellState.Complete.duration:$_duration"
		}

	}
	fun TreeCellState.clone() : TreeCellState {
		return if(this is Complete) Complete(_duration = this.duration)
		else this
	}
}