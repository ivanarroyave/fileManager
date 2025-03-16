package org.example.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
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
    val coroutineScope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // 🔄 Cargar contenido del archivo cuando cambia
    LaunchedEffect(selectedFile) {
        selectedFile?.let { file ->
            textState = file.readText()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        selectedFile?.let { file ->
            // 📄 Barra superior con nombre del archivo y botón de guardar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween // 🔹 Espaciado entre elementos
            ) {
                Text(
                    text = "📄 ${file.name}",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                // 💾 Botón de guardar alineado a la derecha
                Button(
                    onClick = {
                        coroutineScope.launch {
                            file.writeText(textState) // 💾 Guardar el archivo
                        }
                    }
                ) {
                    Text("💾 Guardar")
                }
            }

            // 🔹 Contenedor con scroll vertical y horizontal
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = textState,
                        onValueChange = { newText ->
                            textState = newText // 🔄 Solo actualizar la variable, sin guardar aún
                            fileContents[file] = newText
                        },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black)
                    )
                }

                // 🔹 Ubicar correctamente las barras de desplazamiento dentro de `Box`
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    VerticalScrollbar(
                        modifier = Modifier.fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(verticalScrollState)
                    )
                }

                Box(modifier = Modifier.align(Alignment.BottomStart)) {
                    HorizontalScrollbar(
                        modifier = Modifier.fillMaxWidth(),
                        adapter = rememberScrollbarAdapter(horizontalScrollState)
                    )
                }
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
