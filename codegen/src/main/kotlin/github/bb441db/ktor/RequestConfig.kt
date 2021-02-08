package github.bb441db.ktor

import io.ktor.http.*

sealed class RequestConfig {
    abstract val headers: List<HeaderElement>
    abstract val timeout: TimeoutElement

    abstract operator fun plus(other: RequestConfig): RequestConfig
    abstract operator fun plus(other: Fun): Fun

    open fun distinctHeaders(): List<HeaderElement> {
        return this.headers.distinctBy { it.name }
    }

    data class Class(
        override val headers: List<HeaderElement> = listOf(),
        override val timeout: TimeoutElement = TimeoutElement.Empty
    ) : RequestConfig() {
        override fun plus(other: RequestConfig): RequestConfig {
            if (other is Fun) {
                return other.copy(
                    headers = this.headers + other.headers,
                    timeout = this.timeout + other.timeout,
                )
            }

            return this.copy(
                headers = this.headers + other.headers,
                timeout = this.timeout + other.timeout,
            )
        }

        operator fun plus(other: Class): Class {
            return this.copy(
                headers = this.headers + other.headers,
                timeout = this.timeout + other.timeout,
            )
        }

        override fun plus(other: Fun): Fun {
            return Fun(
                url = other.url,
                method = other.method,
                query = other.query,
                body = other.body,
                attributes = other.attributes,
                headers = this.headers + other.headers,
                timeout = this.timeout + other.timeout,
            )
        }
    }

    data class Fun(
        val url: List<TemplatePart>,
        val method: HttpMethod,
        val body: BodyElement?,
        override val headers: List<HeaderElement> = listOf(),
        val query: List<QueryElement>,
        val attributes: List<AttributeElement> = listOf(),
        override val timeout: TimeoutElement = TimeoutElement.Empty
    ) : RequestConfig() {

        override fun distinctHeaders(): List<HeaderElement> {
            if (this.body != null && this.body.contentType != null) {
                return (listOf(HeaderElement("Content-Type", this.body.contentType)) + this.headers)
                    .distinctBy { it.name }
            }

            return super.distinctHeaders()
        }

        fun distinctAttributes(): List<AttributeElement> {
            return this.attributes.distinctBy { it.name }
        }

        override fun plus(other: RequestConfig): RequestConfig {
            return this.copy(
                headers = this.headers + other.headers,
                attributes = this.attributes,
                query = this.query,
                timeout = this.timeout + other.timeout,
            )
        }

        override fun plus(other: Fun): Fun {
            return this.copy(
                headers = this.headers + other.headers,
                attributes = this.attributes + other.attributes,
                query = this.query + other.query,
                timeout = this.timeout + other.timeout,
            )
        }
    }

    object Empty : RequestConfig() {
        override val headers: List<HeaderElement> = emptyList()
        override val timeout = TimeoutElement.Empty

        override fun plus(other: RequestConfig): RequestConfig {
            return other
        }

        override fun plus(other: Fun): Fun {
            return other
        }
    }
}