package dev.roman.kamyshnikov.codegen.generation

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import dev.roman.kamyshnikov.codegen.generation.models.CodegenDir
import dev.roman.kamyshnikov.codegen.res.R
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.codeInsight.ShortenReferencesFacility
import org.jetbrains.kotlin.idea.base.util.quoteIfNeeded
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Path

class GenerationQueue(
    private val project: Project,
) {
    private val files: MutableMap<CodegenFile.Id, CodegenFile> = mutableMapOf()

    fun addFile(path: Path, file: KtFile) {
        val id = CodegenFile.Id(path = path, fileName = file.name)
        files[id] = CodegenFile(path, file)
    }

    fun writeAllFiles() {
        project.executeWriteCommand(name = R.strings.dialog_title) {
            val shorterReferencesFacility = ShortenReferencesFacility.getInstance()
            val codeStyleManager = CodeStyleManager.getInstance(project)

            files.values
                .map { (targetPath, updatedKtFile) ->
                    val targetDir = targetPath.getOrCreatePsiDirectory(project)

                    when (val existingFile = targetDir.findFile(updatedKtFile.name)) {
                        null -> targetDir.add(updatedKtFile)
                        else -> existingFile.also {
                            val document = existingFile.viewProvider.document
                            document.setText(updatedKtFile.text)
                            PsiDocumentManager.getInstance(project).commitDocument(document)
                        }
                    } as KtFile
                }
                .onEach {
                    shorterReferencesFacility.shorten(it)
                    codeStyleManager.reformat(it)
                }

            files.clear()
        }
    }

    fun getOrCreateInMemoryFile(dir: CodegenDir, fileName: String): KtFile {
        return findInMemoryFile(dir, fileName)
            ?: findExistingFile(dir, fileName)?.copy() as? KtFile
            ?: createInMemoryFile(dir, fileName)
    }

    private fun findInMemoryFile(dir: CodegenDir, fileName: String): KtFile? {
        return files[CodegenFile.Id(path = dir.path, fileName = fileName)]?.file
    }

    private fun findExistingFile(dir: CodegenDir, fileName: String): KtFile? {
        return dir.path.findPsiDirectory(project)?.findFile(fileName) as? KtFile
    }

    private fun createInMemoryFile(dir: CodegenDir, fileName: String): KtFile {
        val packageFqName = dir.fqPackageName
        val file = PsiFileFactory.getInstance(project).createFileFromText(
            fileName,
            KotlinFileType.INSTANCE,
            if (!packageFqName.isRoot) "package ${packageFqName.quoteIfNeeded().asString()} \n\n" else ""
        )
        return file as KtFile
    }
}

private data class CodegenFile(
    val path: Path,
    val file: KtFile,
) {
    @JvmInline
    value class Id private constructor(val value: String) {
        constructor(path: Path, fileName: String) : this("$path/$fileName")
    }
}

private fun Path.findPsiDirectory(project: Project): PsiDirectory? {
    val path = this
    val virtualFile = VfsUtil.findFile(path, false)
    val psiDirectory = virtualFile?.let { PsiManager.getInstance(project).findDirectory(virtualFile) }
    return psiDirectory
}

private fun Path.getOrCreatePsiDirectory(project: Project): PsiDirectory {
    val directoryPath = this.toString()
    val virtualFile = VfsUtil.createDirectoryIfMissing(directoryPath)!!
    val psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFile)!!
    return psiDirectory
}

