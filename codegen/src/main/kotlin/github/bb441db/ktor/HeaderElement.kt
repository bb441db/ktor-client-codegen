package github.bb441db.ktor

data class HeaderElement(val name: String, val value: String) {
    companion object {
        fun parse(header: String): HeaderElement {
            val before = header.substringBefore(':', "").trim()
            val after = header.substringAfter(':', "").trim()

            return HeaderElement(before, after)
        }
    }
}