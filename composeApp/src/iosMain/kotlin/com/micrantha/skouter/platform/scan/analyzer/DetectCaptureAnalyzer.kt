package com.micrantha.skouter.platform.scan.analyzer

import androidx.compose.ui.geometry.Rect
import com.micrantha.skouter.domain.model.DetectClue
import com.micrantha.skouter.domain.model.DetectProof
import com.micrantha.skouter.domain.model.LabelClue
import com.micrantha.skouter.platform.scan.CameraAnalyzerConfig
import com.micrantha.skouter.platform.scan.CameraCaptureAnalyzer
import com.micrantha.skouter.platform.scan.CameraImage
import com.micrantha.skouter.platform.scan.CameraStreamAnalyzer
import com.micrantha.skouter.platform.scan.components.AnalyzerCallback
import com.micrantha.skouter.platform.scan.components.CaptureAnalyzer
import com.micrantha.skouter.platform.scan.components.StreamAnalyzer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetMinX
import platform.CoreGraphics.CGRectGetMinY
import platform.CoreGraphics.CGRectGetWidth
import platform.Vision.VNClassificationObservation
import platform.Vision.VNCoreMLRequest
import platform.Vision.VNRecognizedObjectObservation

typealias ObjectCaptureConfig = CameraAnalyzerConfig<DetectProof, VNCoreMLRequest, VNRecognizedObjectObservation>

actual class DetectCaptureAnalyzer(config: ObjectCaptureConfig = config()) :
    CameraCaptureAnalyzer<DetectProof, VNCoreMLRequest, VNRecognizedObjectObservation>(config),
    CaptureAnalyzer<DetectProof>

class DetectStreamAnalyzer(
    callback: AnalyzerCallback<DetectProof>,
    config: ObjectCaptureConfig = config(),
) : CameraStreamAnalyzer<DetectProof, VNCoreMLRequest, VNRecognizedObjectObservation>(
    config,
    callback
), StreamAnalyzer

@OptIn(ExperimentalForeignApi::class)
private fun config(): ObjectCaptureConfig = object : ObjectCaptureConfig {
    override fun request() = VNCoreMLRequest()

    override val filter = { results: List<*>? ->
        results?.filterIsInstance<VNRecognizedObjectObservation>() ?: emptyList()
    }

    override fun map(
        response: List<VNRecognizedObjectObservation>,
        image: CameraImage
    ): DetectProof {
        return response.map { obj ->
            DetectClue(
                data = Rect(
                    CGRectGetMinX(obj.boundingBox).toFloat(),
                    CGRectGetMinY(obj.boundingBox).toFloat(),
                    CGRectGetWidth(obj.boundingBox).toFloat(),
                    CGRectGetHeight(obj.boundingBox).toFloat()
                ),
                labels = obj.labels.filterIsInstance<VNClassificationObservation>().map {
                    LabelClue(it.identifier, it.confidence)
                }.toSet()
            )
        }.toSet()
    }

}