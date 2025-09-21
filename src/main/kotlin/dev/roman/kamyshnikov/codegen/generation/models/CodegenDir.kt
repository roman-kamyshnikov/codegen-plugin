package dev.roman.kamyshnikov.codegen.generation.models

import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.core.getFqNameWithImplicitPrefixOrRoot
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.nio.file.Path
import kotlin.io.path.Path

@ConsistentCopyVisibility
data class CodegenDir private constructor(
    val path: Path,
    val fqPackageName: FqName,
) {
    companion object {
        fun from(psiDirectory: PsiDirectory): CodegenDir = CodegenDir(
            path = Path(psiDirectory.virtualFile.path),
            fqPackageName = psiDirectory.getFqNameWithImplicitPrefixOrRoot(),
        )
    }

    operator fun plus(name: String): CodegenDir {
        return this.copy(
            path = Path(this.path.toString(), name),
            fqPackageName = this.fqPackageName.child(Name.identifier(name))
        )
    }
}