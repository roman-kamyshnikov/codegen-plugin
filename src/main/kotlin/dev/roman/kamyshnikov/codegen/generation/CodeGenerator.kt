package dev.roman.kamyshnikov.codegen.generation

import dev.roman.kamyshnikov.codegen.generation.models.CodegenDir
import org.jetbrains.kotlin.psi.KtNamedFunction

class CodeGenerator {

    fun generate(
        targetDir: CodegenDir,
        apiFunction: KtNamedFunction,
    ) {
        TODO("Generate code for $apiFunction in $targetDir")
    }
}