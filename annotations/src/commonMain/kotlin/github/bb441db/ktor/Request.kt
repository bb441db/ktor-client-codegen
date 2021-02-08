package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
/**
 * Adds the HTTP method and URL to the request configuration.
 * @param method - Configures the HTTP request to use a HTTP method that is not defined by ktor.
 * @param url - URL or template URL, supports placeholders (e.g "/foo/:bar") that will be replaced by a value parameter during annotation processing.
 */
annotation class Request(val method: String, val url: String)

