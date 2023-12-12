package com.micrantha.skouter.data.thing.model

import com.micrantha.skouter.data.clue.model.ProofData
import com.micrantha.skouter.platform.scan.model.ScanEmbedding
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ThingData(
    val id: String? = null,
    val created_at: String? = null,
    val name: String? = null,
    val imageUrl: String,
    val proof: ProofData? = null,
    val guessed: Boolean? = null,
    val created_by: String,
    val location: String? = null,
    val embedding: JsonElement,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ThingData) return false
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}

typealias ThingRequest = ThingData
typealias ThingResponse = ThingData
typealias ThingListing = ThingData

fun ScanEmbedding.toJsonElement() =
    Json.encodeToJsonElement(this)

fun JsonElement.toImageEmbedding() =
    floatArrayOf(*Json.decodeFromString(this.jsonPrimitive.content))
