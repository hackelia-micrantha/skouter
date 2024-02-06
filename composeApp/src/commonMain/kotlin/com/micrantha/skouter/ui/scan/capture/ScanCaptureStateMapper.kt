package com.micrantha.skouter.ui.scan.capture

import androidx.compose.ui.graphics.painter.BitmapPainter
import com.micrantha.bluebell.data.Log
import com.micrantha.bluebell.data.weightedRandomSample
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.skouter.domain.model.Clue
import com.micrantha.skouter.domain.model.Clues
import com.micrantha.skouter.domain.model.LabelProof
import com.micrantha.skouter.domain.model.LocationClue
import com.micrantha.skouter.domain.model.Proof

class ScanCaptureStateMapper : StateMapper<ScanState, ScanUiState> {

    override fun map(state: ScanState): ScanUiState = try {
        ScanUiState(
            clues = clues(state),
            overlays = overlays(state),
            enabled = state.enabled,
            capture = if (state.enabled.not()) state.image?.toImageBitmap()?.let {
                BitmapPainter(it)
            } else null
        )
    } catch (err: Throwable) {
        Log.e("mapping", err) { "unable to map scan state" }
        throw err
    }

    fun prove(state: ScanState) = Proof(
        clues = Clues(
            labels = state.labels,
            location = state.location?.data?.let { LocationClue(it) },
            colors = state.colors
        ),
        name = "",
        image = state.path!!,
        match = state.match!!.data,
        location = state.location?.point,
        playerID = state.playerID!!
    )

    private fun LabelProof.sample() = this.weightedRandomSample {
        it.confidence.toDouble()
    }

    private fun <T : Clue<*>> format(item: T?, size: Int): String? = when {
        size == 0 || item == null -> null
        size == 1 -> "What: ${item.display()}"
        else -> "What: ${item.display()} +${size - 1}"
    }

    private fun clues(state: ScanState) = mutableListOf<String>().apply {
        state.labels?.let { labels ->
            format(labels.sample(), labels.size)?.let {
                add(it)
            }
        }
        state.colors?.let { colors ->
            format(colors.randomOrNull(), colors.size)?.let {
                add(it)
            }
        }
        state.location?.let { add("Location: ${it.point}") }
    }

    private fun overlays(state: ScanState): List<ScanOverlay> {
        val result = mutableListOf<ScanOverlay>()
        if (state.enabled.not()) return result

        if (state.image != null) {
            state.detection?.let {
                result.add(
                    ScanBox(
                        it.data,
                        it.labels.firstOrNull()?.display() ?: "",
                        imageWidth = state.image.width,
                        imageHeight = state.image.height
                    )
                )
            }
        }
        state.segment?.let {
            result.add(ScanMask(it.data))
        }
        return result
    }
}
