package com.micrantha.skouter.domain.repository

import com.micrantha.skouter.domain.model.Location
import com.micrantha.skouter.domain.model.Proof
import com.micrantha.skouter.domain.model.Thing
import com.micrantha.skouter.domain.model.ThingList
import com.micrantha.skouter.domain.model.ThingMatches
import com.micrantha.skouter.platform.scan.model.ImageEmbedding

interface ThingRepository {

    suspend fun things(playerID: String): Result<ThingList>

    suspend fun thing(thingID: String): Result<Thing>

    suspend fun match(embedding: ImageEmbedding): Result<ThingMatches>

    suspend fun create(
        proof: Proof
    ): Result<Thing>

    suspend fun nearby(
        location: Location.Point,
        distance: Double = 10.0
    ): Result<ThingList>
}