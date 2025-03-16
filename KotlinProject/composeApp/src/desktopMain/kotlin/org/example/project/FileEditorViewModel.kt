package org.example.project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.io.File

class FileEditorViewModel {
    val openFiles = mutableStateListOf<File>()
    val fileContents = mutableStateOf(mutableMapOf<String, String>())

    fun openFile(file: File) {
        if (!openFiles.contains(file)) {
            openFiles.add(file)
            fileContents.value[file.absolutePath] = file.readText()
        }
    }

    fun updateFileContent(filePath: String, newContent: String) {
        fileContents.value[filePath] = newContent
    }

    fun saveFile(filePath: String) {
        val file = openFiles.find { it.absolutePath == filePath }
        file?.writeText(fileContents.value[filePath] ?: "")
    }
}
