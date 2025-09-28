package dev.roman.kamyshnikov.codegen.di

import com.intellij.openapi.project.Project
import dev.roman.kamyshnikov.codegen.generation.CodeGenerator
import dev.roman.kamyshnikov.codegen.generation.DomainModelGenerator
import dev.roman.kamyshnikov.codegen.generation.GenerationQueue
import dev.roman.kamyshnikov.codegen.selectFunction.ApiFunctionProvider
import dev.roman.kamyshnikov.codegen.selectFunction.SelectFunctionDialog
import dev.roman.kamyshnikov.codegen.selectFunction.SelectFunctionDialog.OnFunctionSelected

class ServiceLocator(private val project: Project) {

    private val generationQueue: GenerationQueue = GenerationQueue(project)

    private fun getProject() = project
    private fun getGenerationQueue() = generationQueue

    private fun getApiFunctionProvider() = ApiFunctionProvider(
        project = getProject(),
    )

    fun getSelectFunctionDialog(
        onFunctionSelected: OnFunctionSelected,
    ) = SelectFunctionDialog(
        project = getProject(),
        apiFunctionProvider = getApiFunctionProvider(),
        onFunctionSelected = onFunctionSelected,
    )

    fun getCodeGenerator() = CodeGenerator(
        domainModelGenerator = getDomainModelGenerator(),
        generationQueue = getGenerationQueue(),
    )

    private fun getDomainModelGenerator() = DomainModelGenerator(
        generationQueue = getGenerationQueue(),
    )
}