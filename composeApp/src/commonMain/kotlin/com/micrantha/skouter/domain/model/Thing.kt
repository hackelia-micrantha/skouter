package com.micrantha.skouter.domain.model

import com.micrantha.skouter.platform.scan.model.ScanEmbedding
import kotlinx.datetime.Instant

data class Thing(
    override val id: String,
    override val createdAt: Instant,
    val createdBy: Player.Ref,
    val name: String?,
    val guessed: Boolean,
    val guesses: List<Guess>,
    val imageUrl: ImagePath,
    val clues: Clues,
    val location: Location.Point,
    val embedding: ScanEmbedding
) : Entity, Creatable {

    data class Guess(
        val at: Instant,
        val by: Player.Ref,
        val correct: Boolean
    )

    data class Listing(
        override val id: String,
        val nodeId: String,
        override val createdAt: Instant,
        val name: String?,
        val guessed: Boolean,
        val imageUrl: ImagePath,
    ) : Entity, Creatable

    data class Match(
        override val id: String,
        val image: ScanEmbedding,
        val similarity: Float
    ) : Entity
}

typealias ThingList = List<Thing.Listing>
typealias ThingMatches = List<Thing.Match>

typealias ImagePath = String
