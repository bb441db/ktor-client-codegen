package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Queries(vararg val values: String)