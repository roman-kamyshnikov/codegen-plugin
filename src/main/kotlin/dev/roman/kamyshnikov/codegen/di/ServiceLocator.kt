package dev.roman.kamyshnikov.codegen.di

import com.intellij.openapi.project.Project
import dev.roman.kamyshnikov.codegen.generation.CodeGenerator
import dev.roman.kamyshnikov.codegen.selectFunction.ApiFunctionProvider
import dev.roman.kamyshnikov.codegen.selectFunction.SelectFunctionDialog
import dev.roman.kamyshnikov.codegen.selectFunction.SelectFunctionDialog.OnFunctionSelected

class ServiceLocator(private val project: Project) {

    private fun getProject() = project

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

    fun getCodeGenerator() = CodeGenerator()
}