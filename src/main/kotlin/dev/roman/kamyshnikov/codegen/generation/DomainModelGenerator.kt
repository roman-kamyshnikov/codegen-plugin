package dev.roman.kamyshnikov.codegen.generation

import dev.roman.kamyshnikov.codegen.generation.models.CodegenDir
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtTypeReference

class DomainModelGenerator(
    private val generationQueue: GenerationQueue,
) {
    fun deepCloneParameter(
        parameter: KtParameter,
        targetDir: CodegenDir,
    ): KtParameter {
        TODO()
    }

    fun deepCloneType(
        type: KtTypeReference,
        targetDir: CodegenDir,
    ): KtTypeReference {
        TODO()
    }
}