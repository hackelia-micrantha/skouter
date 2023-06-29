package com.micrantha.skouter.ui.scan.preview

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.skouter.domain.model.ColorProof
import com.micrantha.skouter.domain.model.DetectClue
import com.micrantha.skouter.domain.model.DetectProof
import com.micrantha.skouter.domain.model.LabelProof
import com.micrantha.skouter.domain.model.Location
import com.micrantha.skouter.domain.model.MatchClue
import com.micrantha.skouter.domain.model.MatchProof
import com.micrantha.skouter.domain.model.SegmentClue
import com.micrantha.skouter.domain.model.SegmentProof
import com.micrantha.skouter.platform.CameraImage
import okio.Path

data class ScanState(
    val labels: LabelProof? = null,
    val location: Location? = null,
    val colors: ColorProof? = null,
    val image: CameraImage? = null,
    val enabled: Boolean = true,
    val detection: DetectClue? = null,
    val segment: SegmentClue? = null,
    val match: MatchClue? = null,
    val path: Path? = null,
    val playerID: String? = null
)

data class ScanUiState(
    val clues: List<String>,
    val overlays: List<ScanOverlay>,
    val enabled: Boolean
)

sealed class ScanAction : Action {
    interface ScanSavable {
        val path: Path
    }

    object SaveScan : ScanAction()

    object EditScan : ScanAction()

    object SaveError : ScanAction()

    data class EditSaved(override val path: Path) : ScanAction(), ScanSavable
    data class ImageSaved(override val path: Path) : ScanAction(), ScanSavable

    data class ImageCaptured(val image: CameraImage) : ScanAction()

    data class ScannedLabels(
        val labels: LabelProof
    ) : ScanAction()

    data class ScannedColors(
        val colors: ColorProof
    ) : ScanAction()

    data class ScannedObjects(
        val detections: DetectProof
    ) : ScanAction()

    data class ScannedSegments(
        val segments: SegmentProof
    ) : ScanAction()

    data class ScannedMatch(
        val matches: MatchProof
    ) : ScanAction()
}

sealed interface ScanOverlay

data class ScanMask(
    val mask: ImageBitmap,
) : ScanOverlay

data class ScanBox(
    val rect: Rect,
    val label: String,
    val scale: Float
) : ScanOverlay
