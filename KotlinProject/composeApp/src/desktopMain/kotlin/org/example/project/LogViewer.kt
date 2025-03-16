package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun LogViewer() {
    var logs by remember { mutableStateOf(listOf("Log inicial...")) }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        logs.forEach { log ->
            Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Text(log, modifier = Modifier.weight(1f))
                Button(onClick = { exportLog(log) }) {
                    Text("Exportar")
                }
            }
            Divider()
        }
    }
}

fun exportLog(log: String) {
    val logFile = File("log_${System.currentTimeMillis()}.txt")
    logFile.writeText(log)
    println("Log exportado a ${logFile.absolutePath}")
}
