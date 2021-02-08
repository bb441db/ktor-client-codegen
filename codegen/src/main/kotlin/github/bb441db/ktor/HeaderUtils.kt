package github.bb441db.ktor

object HeaderUtils {
    fun parse(header: String): Pair<String, String> {
        val before = header.substringBefore(':', "").trim()
        val after = header.substringAfter(':', "").trim()

        return before to after
    }
}