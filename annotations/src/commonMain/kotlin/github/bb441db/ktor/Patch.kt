package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
/**
 * Short-hand version of [Request] for HTTP method PATCH
 */
annotation class Patch(val url: String)