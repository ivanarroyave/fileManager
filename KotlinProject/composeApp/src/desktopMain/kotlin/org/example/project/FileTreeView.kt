package org.example.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JOptionPane

var clipboardFile: File? = null
var isCutOperation = false

@Composable
fun FileTreeView(viewModel: FileEditorViewModel, onFileSelected: (File) -> Unit, refreshTrigger: Int) {
    val basePath = "C:\\Users\\pc\\Desktop\\root"
    var expandedNodes by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    var rootNode by remember { mutableStateOf(buildFileTree(File(basePath), expandedNodes)) }

    // 🔄 Reactivar el árbol cuando cambia refreshTrigger
    LaunchedEffect(refreshTrigger) {
        rootNode = buildFileTree(File(basePath), expandedNodes)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            FileTreeNode(
                node = rootNode,
                viewModel = viewModel,
                expandedNodes = expandedNodes,
                onRefresh = { rootNode = buildFileTree(File(basePath), expandedNodes) },
                onFileSelected = onFileSelected
            )
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(rememberScrollState())
        )

        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomStart),
            adapter = rememberScrollbarAdapter(rememberScrollState())
        )
    }
}



fun buildFileTree(file: File, expandedNodes: MutableMap<String, Boolean> = mutableMapOf()): FileNode {
    val isExpanded = expandedNodes.getOrDefault(file.absolutePath, false)
    val children = if (isExpanded && file.isDirectory) {
        file.listFiles()?.sortedBy { it.name }?.map { buildFileTree(it, expandedNodes) } ?: emptyList()
    } else {
        emptyList()
    }
    return FileNode(file, isExpanded, children)
}

@Composable
fun FileTreeNode(
    node: FileNode,
    viewModel: FileEditorViewModel,
    expandedNodes: MutableMap<String, Boolean>,
    onRefresh: () -> Unit,
    onFileSelected: (File) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                // 📌 Guardar posición y mostrar menú
                                menuPosition = Offset(event.changes[0].position.x, event.changes[0].position.y)
                                showMenu = true
                            }
                        }
                    }
                }
                .clickable {
                    if (node.file.isDirectory) {
                        expandedNodes[node.file.absolutePath] = !(expandedNodes[node.file.absolutePath] ?: false)
                        onRefresh()
                    } else {
                        onFileSelected(node.file) // 📌 Solo si es archivo
                    }
                }
                .padding(4.dp)
        ) {
            val icon = when {
                node.file.isDirectory && expandedNodes[node.file.absolutePath] == true -> "📂"
                node.file.isDirectory -> "📁"
                else -> "📄"
            }

            Text("$icon ${node.file.name}")
        }

        // 📌 Mostrar menú contextual solo cuando se activa
        if (showMenu) {
            Popup(
                onDismissRequest = { showMenu = false }
            ) {
                ContextMenu(node.file, menuPosition, onDismiss = { showMenu = false }, onRefresh)
            }
        }

        // 🔹 Mostrar hijos solo si el nodo está expandido
        if (expandedNodes[node.file.absolutePath] == true) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                node.children.forEach { child ->
                    FileTreeNode(
                        node = child,
                        viewModel = viewModel,
                        expandedNodes = expandedNodes,
                        onRefresh = onRefresh,
                        onFileSelected = onFileSelected
                    )
                }
            }
        }
    }
}


@Composable
fun ContextMenu(
    file: File,
    position: Offset,
    onDismiss: () -> Unit, // 🔹 Se usará para cerrar el menú
    onRefresh: () -> Unit
) {
    Popup(
        offset = IntOffset(position.x.toInt(), position.y.toInt()),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .width(180.dp) // 🔹 Definir ancho máximo
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray)
                .shadow(4.dp)
                .padding(vertical = 4.dp)
        ) {
            if (file.isDirectory) {
                MenuItem("📁 Nueva Carpeta", onDismiss) { createFolder(file, onRefresh) }
                MenuItem("📄 Nuevo Archivo", onDismiss) { createFile(file, onRefresh) }
            }
            MenuItem("✂️ Cortar", onDismiss) {
                clipboardFile = file
                isCutOperation = true
            }
            MenuItem("📋 Copiar", onDismiss) {
                clipboardFile = file
                isCutOperation = false
            }
            MenuItem("📂 Pegar", onDismiss) {
                clipboardFile?.let { pasteFile(it, file, onRefresh) }
            }
            MenuItem("✏️ Renombrar", onDismiss) { renameFile(file, onRefresh) }
            MenuItem("🗑️ Eliminar", onDismiss) { deleteFile(file, onRefresh) }
        }
    }
}

@Composable
fun MenuItem(label: String, onDismiss: () -> Unit, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
                onDismiss() // 🔹 Cerrar el menú inmediatamente después de hacer clic
            }
            .padding(8.dp),
        color = Color.Black
    )
}


fun createFolder(parent: File, onRefresh: () -> Unit) {
    val folderName = JOptionPane.showInputDialog("Ingrese el nombre de la nueva carpeta:")
    if (folderName.isNullOrBlank()) return

    val newFolder = File(parent, folderName)
    if (newFolder.mkdir()) {
        onRefresh()
    } else {
        JOptionPane.showMessageDialog(null, "No se pudo crear la carpeta.")
    }
}

fun renameFile(file: File, onRefresh: () -> Unit) {
    val newName = JOptionPane.showInputDialog("Ingrese el nuevo nombre para '${file.name}':")
    if (newName.isNullOrBlank()) return

    val newFile = File(file.parent, newName)
    if (file.renameTo(newFile)) {
        onRefresh()
    } else {
        JOptionPane.showMessageDialog(null, "Error al renombrar el archivo/carpeta.")
    }
}

fun deleteFile(file: File, onRefresh: () -> Unit) {
    val confirm = JOptionPane.showConfirmDialog(
        null, "¿Seguro que quieres eliminar '${file.name}'?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION
    )

    if (confirm == JOptionPane.YES_OPTION) {
        if (file.deleteRecursively()) {
            onRefresh()
        } else {
            JOptionPane.showMessageDialog(null, "No se pudo eliminar el archivo/carpeta.")
        }
    }
}

fun createFile(parent: File, onRefresh: () -> Unit) {
    val fileName = JOptionPane.showInputDialog("Ingrese el nombre del nuevo archivo:")
    if (fileName.isNullOrBlank()) return

    val newFile = File(parent, fileName)

    if (newFile.exists()) {
        JOptionPane.showMessageDialog(null, "El archivo ya existe.")
        return
    }

    try {
        if (newFile.createNewFile()) {
            onRefresh()  // 🔄 Refrescar el árbol después de crear el archivo
        } else {
            JOptionPane.showMessageDialog(null, "No se pudo crear el archivo.")
        }
    } catch (e: Exception) {
        JOptionPane.showMessageDialog(null, "Error: ${e.message}")
    }
}

fun pasteFile(source: File, destination: File, onRefresh: () -> Unit) {
    if (!destination.isDirectory) {
        JOptionPane.showMessageDialog(null, "No se puede pegar aquí. Seleccione una carpeta.")
        return
    }

    val newFile = File(destination, source.name)

    if (newFile.exists()) {
        JOptionPane.showMessageDialog(null, "El archivo/carpeta ya existe en el destino.")
        return
    }

    try {
        if (isCutOperation) {
            source.moveRecursively(newFile) // 🔄 Mover carpeta con subarchivos y subcarpetas
            clipboardFile = null // 🔄 Limpiar clipboard después de cortar
        } else {
            source.copyRecursively(newFile) // 🔄 Copiar carpeta con subarchivos y subcarpetas
        }

        onRefresh() // 🔄 Refrescar el árbol después de pegar

    } catch (e: Exception) {
        JOptionPane.showMessageDialog(null, "Error al pegar el archivo/carpeta: ${e.message}")
    }
}

fun File.copyRecursively(target: File) {
    if (this.isDirectory) {
        if (!target.exists()) target.mkdirs()
        this.listFiles()?.forEach { file ->
            file.copyRecursively(File(target, file.name))
        }
    } else {
        Files.copy(this.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}

fun File.moveRecursively(target: File) {
    this.copyRecursively(target)
    this.deleteRecursively()
}
