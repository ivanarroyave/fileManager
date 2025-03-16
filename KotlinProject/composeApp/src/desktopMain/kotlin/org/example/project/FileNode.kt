package org.example.project

import java.io.File

data class FileNode(
    val file: File,
    val isExpanded: Boolean = false,
    val children: List<FileNode> = emptyList()
)
