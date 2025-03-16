package org.example.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogViewer() {
    val logDir = File("logs")
    if (!logDir.exists()) logDir.mkdirs() // 📂 Crear carpeta de logs si no existe

    var logFiles by remember { mutableStateOf(loadLogFiles(logDir)) } // ✅ Lista de archivos de log
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // 📂 Botón fijo en la parte superior
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { openLogFolder(logDir) }
            ) {
                Text("📂 Abrir Carpeta")
            }
        }

        // 🔽 Sección desplazable de logs
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                logFiles.forEach { logFile ->
                    LogEntry(logFile)
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
    }

    // 🔄 Actualizar lista de logs automáticamente
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // 🕒 Revisar cada segundo si hay nuevos logs
            logFiles = loadLogFiles(logDir)
        }
    }
}

@Composable
fun LogEntry(logFile: File) {
    Card(
        backgroundColor = Color.LightGray.copy(alpha = 0.2f), // 🎨 Fondo para diferenciar cada log
        modifier = Modifier.fillMaxWidth().padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(logFile.name, modifier = Modifier.weight(1f)) // 📄 Nombre del log

            Button(onClick = { openLogFile(logFile) }) { // 🔍 Botón para abrir el log
                Text("📖 Abrir")
            }
        }
    }
}

// 📂 Cargar lista de archivos de log
fun loadLogFiles(logDir: File): List<File> {
    return logDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
}

// 📝 Guardar log en un archivo independiente
fun saveLogEntry(entry: String) {
    val logDir = File("logs")
    if (!logDir.exists()) logDir.mkdirs()

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val logFile = File(logDir, "log_$timestamp.txt")

    logFile.writeText(entry)
}

// 🔍 Abrir archivo de log con el editor de texto predeterminado
fun openLogFile(logFile: File) {
    try {
        Desktop.getDesktop().open(logFile)
    } catch (e: Exception) {
        println("Error al abrir el archivo: ${e.message}")
    }
}

// 📂 Abrir la carpeta de logs en el explorador de archivos
fun openLogFolder(logDir: File) {
    try {
        Desktop.getDesktop().open(logDir)
    } catch (e: Exception) {
        println("Error al abrir la carpeta: ${e.message}")
    }
}
