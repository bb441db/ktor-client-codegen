package github.bb411db.ktor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import github.bb441db.ktor.KtorProcessor
import org.assertj.core.api.Assertions

@ExperimentalStdlibApi
@KotlinPoetMetadataPreview
abstract class CompilationTests {
    abstract val debug: Boolean

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            sources = sourceFiles.toList()
            annotationProcessors = listOf(KtorProcessor())
            inheritClassPath = true

            if (debug) {
                messageOutputStream = System.out
            }
        }.compile().also {
            if (debug) {
                it.generatedFiles
                    .filter { file -> file.extension == "kt" }
                    .forEach { file ->
                        println("Generated file: ${file.name}")
                        print(file.readText())
                        println("-".repeat(50))
                    }
            }
        }
    }

    fun assertEquals(source: String, implementation: String, className: String) {
        val result = compile(SourceFile.kotlin("${className}.kt", source))

        Assertions
            .assertThat(result.generatedFiles.first { it.name == "${className}.kt" })
            .hasContent(implementation)
    }
}