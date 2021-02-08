package github.bb441db.ktor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@KotlinPoetMetadataPreview
@ExperimentalStdlibApi
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class KtorProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Ktor::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(Ktor::class.java)

        for (element in elements) {
            if (element is TypeElement) {
                val generator = CodeGenerator(element, processingEnv)
                generator
                    .generate()
                    .writeTo(processingEnv.filer)
            }
        }

        return true
    }
}