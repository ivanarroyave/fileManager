package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun App() {
    val editorViewModel = remember { FileEditorViewModel() }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) } // 🔄 Para forzar actualización del árbol

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            TopAppBar(title = { Text("Gestor de Archivos") })

            Button(
                onClick = { refreshTrigger++ }, // 🔄 Forzar actualización del árbol
                modifier = Modifier.padding(8.dp)
            ) {
                Text("🔄 Actualizar")
            }

            // 🔹 Línea divisoria debajo del botón
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.LightGray.copy(alpha = 0.4f), shape = RoundedCornerShape(50))
            )

            var treeWidth by remember { mutableStateOf(0.25f) }
            var editorHeight by remember { mutableStateOf(0.75f) }

            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(treeWidth)
                        .padding(8.dp)
                ) {
                    FileTreeView(
                        viewModel = editorViewModel,
                        onFileSelected = { file: File -> selectedFile = file },
                        refreshTrigger = refreshTrigger // 🔄 Se pasa el trigger al árbol
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                treeWidth = (treeWidth + dragAmount / 1000f).coerceIn(0.15f, 0.5f)
                            }
                        }
                        .background(Color.LightGray.copy(alpha = 0.4f), shape = RoundedCornerShape(50))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(editorHeight)
                            .padding(8.dp)
                    ) {
                        FileEditor(viewModel = editorViewModel, selectedFile = selectedFile)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    editorHeight = (editorHeight + dragAmount / 1000f).coerceIn(0.3f, 0.85f)
                                }
                            }
                            .background(Color.LightGray.copy(alpha = 0.4f), shape = RoundedCornerShape(50))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f - editorHeight)
                            .padding(8.dp)
                    ) {
                        LogViewer()
                    }
                }
            }
        }
    }
}


@Composable
fun DividerHandle(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val handleWidth = size.width * 0.6f // 🔹 60% del ancho del divisor
        val handleHeight = size.height * 0.6f // 🔹 60% de la altura del divisor
        val centerX = size.width / 2 - handleWidth / 2
        val centerY = size.height / 2 - handleHeight / 2

        drawRoundRect(
            color = Color.LightGray.copy(alpha = 0.6f), // ✅ Mismo color que las líneas
            topLeft = Offset(centerX, centerY),
            size = androidx.compose.ui.geometry.Size(handleWidth, handleHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()) // 🔹 Bordes suavizados
        )
    }
}

