package dev.roman.kamyshnikov.codegen.di

import com.intellij.openapi.project.Project
import dev.roman.kamyshnikov.codegen.selectFunction.ApiFunctionProvider
import dev.roman.kamyshnikov.codegen.selectFunction.SelectFunctionDialog
import dev.roman.kamyshnikov.codegen.selectFunction.SelectFunctionDialog.OnFunctionSelected

object ServiceLocator {
    private var project: Project? = null

    fun init(project: Project) {
        this.project = project
    }

    private fun getProject() = requireNotNull(project)

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
}