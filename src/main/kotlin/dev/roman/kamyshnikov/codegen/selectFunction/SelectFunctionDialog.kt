package dev.roman.kamyshnikov.codegen.selectFunction

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import dev.roman.kamyshnikov.codegen.res.R
import org.jetbrains.kotlin.psi.KtNamedFunction
import javax.swing.JComponent

class SelectFunctionDialog(
    private val project: Project,
    private val apiFunctionProvider: ApiFunctionProvider,
    private val onFunctionSelected: OnFunctionSelected,
) : DialogWrapper(true) {

    fun interface OnFunctionSelected {
        operator fun invoke(apiFunction: KtNamedFunction)
    }

    init {
        title = R.strings.dialog_title
        super.init()

        isOKActionEnabled = true
    }

    private lateinit var functionNameTextField: TextFieldWithAutoCompletion<KtNamedFunction>

    private var selectedApiFunction: KtNamedFunction? = null

    override fun doOKAction() {
        val function = requireNotNull(selectedApiFunction)
        close(OK_EXIT_CODE)
        onFunctionSelected(function)
    }

    override fun doValidate(): ValidationInfo? {
        selectedApiFunction = apiFunctionProvider.findFunction(functionNameTextField.text)

        return when {
            selectedApiFunction == null -> ValidationInfo(R.strings.invalid_function_error, functionNameTextField)
            else -> null
        }
    }

    override fun createCenterPanel(): JComponent {
        functionNameTextField = TextFieldWithAutoCompletion(
            project,
            object : TextFieldWithAutoCompletionListProvider<KtNamedFunction>(apiFunctionProvider.getVariants()) {
                override fun getLookupString(item: KtNamedFunction): String {
                    return requireNotNull(apiFunctionProvider.functionAsString(item))
                }
            },
            false,
            R.strings.function_name_text_field_initial_value,
        )

        return panel {
            row { label(R.strings.function_name_text_field_label) }
            row {
                cell(functionNameTextField)
                    .align(AlignX.FILL)
                    .validationOnInput {
                        isOKActionEnabled = true
                        return@validationOnInput null
                    }
            }
        }
    }
}