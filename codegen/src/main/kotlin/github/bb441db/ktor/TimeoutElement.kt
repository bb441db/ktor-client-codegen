package github.bb441db.ktor

data class TimeoutElement(val connect: Long = -1, val request: Long = -1, val socket: Long = -1) {
    operator fun plus(other: TimeoutElement): TimeoutElement {
        return TimeoutElement(
            connect = if (other.connect > -1) other.connect else this.connect,
            request = if (other.request > -1) other.request else this.request,
            socket = if (other.socket > -1) other.socket else this.socket,
        )
    }

    companion object {
        val Empty = TimeoutElement()

        fun from(annotation: Timeout): TimeoutElement {
            return TimeoutElement(connect = annotation.connect, request = annotation.request, socket = annotation.socket)
        }
    }
}