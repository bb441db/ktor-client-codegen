package github.bb411db.ktor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import org.junit.jupiter.api.Test

@ExperimentalStdlibApi
@KotlinPoetMetadataPreview
class SimpleMethodRequestConfigurationTests : CompilationTests() {
    override val debug: Boolean = true

    @Test
    fun `Request with Unit return type`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    suspend fun bar()
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import kotlin.Unit
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun bar() = this.client.`get`<Unit> {
                    url("/bar")
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies header defined on method`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    @Header("Content-Type", "application/json")
                    suspend fun bar(): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.header
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun bar(): Int = this.client.`get` {
                    url("/bar")
                    header("Content-Type", "application/json")
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies headers defined on method`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    @Headers(
                        "Content-Type: application/json",
                        "Accept-Language: en-US",
                    )
                    suspend fun bar(): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.headers
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun bar(): Int = this.client.`get` {
                    url("/bar")
                    headers {
                      append("Content-Type", "application/json")
                      append("Accept-Language", "en-US")
                    }
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies query defined as value parameter(s)`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    suspend fun bar(@Query search: String, @Query("foo") sort: String): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.parameter
                import io.ktor.client.request.url
                import kotlin.Int
                import kotlin.String
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun bar(search: String, sort: String): Int = this.client.`get` {
                    url("/bar")
                    parameter("search", search)
                    parameter("foo", sort)
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies attributes defined as value parameter(s)`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    suspend fun bar(@Attr foo: Int, @Attr("notBar") bar: Int): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import io.ktor.util.AttributeKey
                import kotlin.Int
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun bar(foo: Int, bar: Int): Int = this.client.`get` {
                    url("/bar")
                    setAttributes {
                      put(AttributeKey("foo"), foo)
                      put(AttributeKey("notBar"), bar)
                    }
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies timeout defined on method`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    @Timeout(request = 2500)
                    suspend fun request(): Int
                    
                    @Get("/bar")
                    @Timeout(socket = 2500)
                    suspend fun socket(): Int
                    
                    @Get("/bar")
                    @Timeout(connect = 2500)
                    suspend fun connect(): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.features.timeout
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import kotlin.Int
                
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun connect(): Int = this.client.`get` {
                    url("/bar")
                    timeout {
                      connectTimeoutMillis = 2500
                    }
                  }
                
                  public override suspend fun request(): Int = this.client.`get` {
                    url("/bar")
                    timeout {
                      requestTimeoutMillis = 2500
                    }
                  }
                
                  public override suspend fun socket(): Int = this.client.`get` {
                    url("/bar")
                    timeout {
                      socketTimeoutMillis = 2500
                    }
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies body defined on method`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    suspend fun foo(@Body bar: Int): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun foo(bar: Int): Int = this.client.`get` {
                    url("/bar")
                    body = bar
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Applies body with content-type defined on method`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar")
                    suspend fun foo(@Body("application/json") bar: Int): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.header
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun foo(bar: Int): Int = this.client.`get` {
                    url("/bar")
                    body = bar
                    header("Content-Type", "application/json")
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Uses correct HTTP method`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Delete("/bar")
                    suspend fun delete(): Int
                    
                    @Get("/bar")
                    suspend fun get(): Int
                    
                    @Head("/bar")
                    suspend fun head(): Int
                    
                    @Patch("/bar")
                    suspend fun patch(): Int
                    
                    @Post("/bar")
                    suspend fun post(): Int
                    
                    @Put("/bar")
                    suspend fun put(): Int
                    
                    @Request(method = "CUSTOM", url = "/bar")
                    suspend fun request(): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.delete
                import io.ktor.client.request.head
                import io.ktor.client.request.patch
                import io.ktor.client.request.post
                import io.ktor.client.request.put
                import io.ktor.client.request.request
                import io.ktor.client.request.url
                import io.ktor.http.HttpMethod
                import kotlin.Int
                
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun delete(): Int = this.client.delete {
                    url("/bar")
                  }
                
                  public override suspend fun `get`(): Int = this.client.`get` {
                    url("/bar")
                  }
                
                  public override suspend fun head(): Int = this.client.head {
                    url("/bar")
                  }
                
                  public override suspend fun patch(): Int = this.client.patch {
                    url("/bar")
                  }
                
                  public override suspend fun post(): Int = this.client.post {
                    url("/bar")
                  }
                
                  public override suspend fun put(): Int = this.client.put {
                    url("/bar")
                  }
                
                  public override suspend fun request(): Int = this.client.request {
                    method = HttpMethod("CUSTOM")
                    url("/bar")
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Builds correct URL when using parameters`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService {
                    @Get("/bar/:id")
                    suspend fun foo(id: Int): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl(
                  private val client: HttpClient
                ) : FooService {
                  public override suspend fun foo(id: Int): Int = this.client.`get` {
                    url(buildString {
                      append("/bar/")
                      append(id)
                    })
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Interface with generic type`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService<T> {
                    @Get("/bar")
                    suspend fun foo(): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl<T>(
                  private val client: HttpClient
                ) : FooService<T> {
                  public override suspend fun foo(): Int = this.client.`get` {
                    url("/bar")
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }

    @Test
    fun `Interface with generic type with upper bound`() {
        assertEquals(
            source = """
                package github.bb441db.ktor
            
                @Ktor
                interface FooService<T : Int> {
                    @Get("/bar")
                    suspend fun foo(): Int
                }
            """.trimIndent(),
            implementation = """
                package github.bb441db.ktor

                import io.ktor.client.HttpClient
                import io.ktor.client.request.`get`
                import io.ktor.client.request.url
                import kotlin.Int
    
                public class FooServiceImpl<T : Int>(
                  private val client: HttpClient
                ) : FooService<T> {
                  public override suspend fun foo(): Int = this.client.`get` {
                    url("/bar")
                  }
                }
            """.trimIndent(),
            className = "FooService"
        )
    }
}