package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Timeout(val connect: Long = -1, val request: Long = -1, val socket: Long = -1)