# Ktor Code Generation

Generates code for [ktor client](https://ktor.io/docs/clients-index.html) HTTP requests 

## Example

```kt
data class Bar(val id: Int)

@Ktor
@Header("Accept", "application/json")
interface FooService {
    @Get("/bar/:id")
    suspend fun bar(id: Int): Bar
}

// Generates

import io.ktor.client.HttpClient
import io.ktor.client.request.`get`
import io.ktor.client.request.url
import kotlin.Int

public class FooServiceImpl(
  private val client: HttpClient
) : FooService {
  public override suspend fun bar(id: Int): Bar = this.client.`get` {
    url(buildString {
      append("/bar/")
      append(id)
    })
    header("Accept", "application/json")
  }
}
```

## Reified generic type response

```kt
data class Bar(val id: Int)

@Ktor
@Header("Accept", "application/json")
interface FooService {
    @Get("/bar/:id")
    suspend fun bar(id: Int): HttpResponse
}

suspend inline fun <reified T: Any> FooService.inlineBar(id: Int): T = bar(id).receive()
```

## Annotations
|Name|Allowed|Description|
|---|---|---|
|`@Ktor`|interface|Marks interface for code generation|
|`@Request(method: String, url: String)`|method|Define the HTTP method and URL)|
|`@Delete(url: String)`|method|Short-handed version of `@Request`|
|`@Get(url: String)`|method|Short-handed version of `@Request`|
|`@Head(url: String)`|method|Short-handed version of `@Request`|
|`@Options(url: String)`|method|Short-handed version of `@Request`|
|`@Patch(url: String)`|method|Short-handed version of `@Request`|
|`@Post(url: String)`|method|Short-handed version of `@Request`|
|`@Put(url: String)`|method|Short-handed version of `@Request`|
|`@Header(name: String, value: String)`|method, interface|Single HTTP header|
|`@Headers(vararg values: String)`|method, interface|Multiple HTTP headers|
|`@Param(name: String = "")`|method parameter|Override the name of a parameter, this is used when finding a parameter for URL template placeholders, e.g `/bar/:id`|
|`@Timeout(connect: Long = -1, request: Long = -1, socket: Long = -1)`|method|Define connect, socket and/or request timeouts|