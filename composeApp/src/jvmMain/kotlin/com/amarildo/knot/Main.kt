package com.amarildo.knot

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.amarildo.knot.view.Knot

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Knot",
    ) {
        Knot()
    }
}
