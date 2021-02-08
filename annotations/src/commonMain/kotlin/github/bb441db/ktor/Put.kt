package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
/**
 * Short-hand version of [Request] for HTTP method PUT
 */
annotation class Put(val url: String)