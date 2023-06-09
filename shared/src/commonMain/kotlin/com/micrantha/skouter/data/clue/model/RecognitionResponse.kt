package com.micrantha.skouter.data.clue.model

import kotlinx.serialization.Serializable

@Serializable
data class RecognitionResponse(
    val labels: List<Label>
) {
    @Serializable
    data class Label(
        val label: String,
        val probability: Float,
    )
}
