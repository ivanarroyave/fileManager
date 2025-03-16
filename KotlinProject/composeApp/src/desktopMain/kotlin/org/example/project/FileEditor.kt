package org.example.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun FileEditor(viewModel: FileEditorViewModel, selectedFile: File?) {
    var fileContents by remember { mutableStateOf(mutableMapOf<File, String>()) }
    var textState by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // ✅ Carga inicial del archivo sin bloquear la UI
    LaunchedEffect(selectedFile) {
        selectedFile?.let { file ->
            try {
                if (file.exists() && file.isFile && file.canRead()) {
                    file.inputStream().bufferedReader().use { reader ->
                        textState = reader.readText()
                    }
                    errorMessage = null
                } else {
                    errorMessage = "⚠️ No se puede leer el archivo: Permiso denegado."
                }
            } catch (e: Exception) {
                errorMessage = "⚠️ Error al abrir el archivo: ${e.message}"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        selectedFile?.let { file ->
            // 🔹 Mostrar el nombre del archivo en la parte superior
            Text(
                text = "📄 ${file.name}",
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                    .padding(8.dp)
            )

            // 🔹 Contenedor con scroll vertical y horizontal
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(8.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        BasicTextField(
                            value = textState,
                            onValueChange = { newText ->
                                textState = newText
                                fileContents[file] = newText

                                // 🔄 Guardado automático en un hilo separado
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        file.writeText(newText)
                                    } catch (e: Exception) {
                                        errorMessage = "⚠️ Error al guardar: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = LocalTextStyle.current.copy(color = Color.Black)
                        )
                    }
                }

                // 🔹 Ubicar correctamente las barras de desplazamiento dentro de `Box`
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(verticalScrollState)
                )

                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomStart),
                    adapter = rememberScrollbarAdapter(horizontalScrollState)
                )
            }
        } ?: Text(
            "Ningún archivo abierto",
            modifier = Modifier.fillMaxSize().padding(16.dp),
            style = MaterialTheme.typography.h6.copy(color = Color.Gray)
        )
    }
}


@Composable
fun SyntaxHighlighter(text: String, fileType: String): AnnotatedString {
    return buildAnnotatedString {
        val words = text.split(" ")
        for (word in words) {
            when {
                word.startsWith("{") || word.startsWith("[") -> withStyle(style = SpanStyle(Color.Blue)) { append(word) }
                word.contains(":") -> withStyle(style = SpanStyle(Color.Red)) { append(word) }
                word.contains("\"") -> withStyle(style = SpanStyle(Color.Green)) { append(word) }
                else -> append(word)
            }
            append(" ")
        }
    }
}

@Composable
fun FileWatcher(file: File) {
    var lastModified by remember { mutableStateOf(file.lastModified()) }
    var content by remember { mutableStateOf(file.readText()) }

    LaunchedEffect(lastModified) {
        while (true) {
            delay(1000) // Verifica cambios cada segundo
            if (file.lastModified() != lastModified) {
                lastModified = file.lastModified()
                content = file.readText()
            }
        }
    }

    Text(content, modifier = Modifier.fillMaxSize().padding(8.dp))
}
