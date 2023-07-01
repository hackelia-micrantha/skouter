package com.micrantha.skouter.ui.scan.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import com.micrantha.skouter.ui.scan.preview.ScanBox
import com.micrantha.skouter.ui.scan.preview.ScanMask


@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawScanBox(
    data: ScanBox,
    matrix: Matrix,
    textStyle: TextStyle,
    measurer: TextMeasurer
) {
    matrix.reset()
    matrix.scale(data.scale, data.scale)

    val bounds = matrix.map(data.rect)

    drawOutline(
        outline = Outline.Rectangle(bounds),
        color = Color.Red,
        style = Stroke(1f)
    )
    drawText(
        textMeasurer = measurer,
        text = data.label,
        topLeft = bounds.topLeft,
        style = textStyle
    )
}

fun DrawScope.drawScanMask(
    data: ScanMask
) {
    drawImage(data.mask)
}