package com.micrantha.skouter.domain.model

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap

data class Clues(
    val labels: Set<LabelClue>? = null,
    val location: LocationClue? = null, // TODO: Geofence
    val colors: Set<ColorClue>? = null
)


typealias LabelProof = Set<LabelClue>

typealias ColorProof = Set<ColorClue>

typealias LocationProof = LocationClue

typealias DetectProof = Set<DetectClue>

typealias SegmentProof = List<SegmentClue>


sealed interface Clue<T> {
    val data: T

    fun display() = data.toString()
}

sealed interface SortedClue<T : Comparable<T>> : Clue<T>, Comparable<SortedClue<T>> {
    override fun compareTo(other: SortedClue<T>) = data.compareTo(other.data)
}

data class ColorClue(
    override val data: String,
) : Clue<String> {
    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is ColorClue) {
            return other.data == data
        }
        return super.equals(other)
    }
}

data class LabelClue(
    override val data: String,
    val confidence: Float
) : SortedClue<String> {

    override fun compareTo(other: SortedClue<String>): Int {
        return if (other is LabelClue) {
            other.confidence.compareTo(confidence)
        } else {
            data.compareTo(other.data)
        }
    }

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is LabelClue) {
            return data == other.data
        }
        return super.equals(other)
    }
}

data class LocationClue(
    override val data: Location.Data, // TODO: make a geofence area
) : SortedClue<Location.Data>

data class RhymeClue(
    override val data: String,
) : Clue<String>

data class DetectClue(
    val rect: Rect,
    override val data: Int,
    val labels: LabelProof
) : Clue<Int> {

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is DetectClue) {
            return data == other.data
        }
        return super.equals(other)
    }
}

data class SegmentClue(
    val data: ImageBitmap,
)
