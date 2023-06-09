package com.micrantha.skouter.data.system

import com.micrantha.skouter.data.account.model.CurrentSession
import com.micrantha.skouter.data.system.mapping.copy
import com.micrantha.skouter.data.system.source.LocationLocalSource
import com.micrantha.skouter.domain.model.Location
import com.micrantha.skouter.domain.repository.LocationRepository
import dev.icerock.moko.geo.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.updateAndGet

class LocationDataRepository(
    private val localSource: LocationLocalSource,
    private val currentSession: CurrentSession,
    //private val geocodeSource: GeoCodeSource,
) : LocationRepository {
    private val location = MutableStateFlow<Location?>(null)

    override suspend fun start() {
        localSource.startTracking()
        flowToCurrentSession()
        exponentialBackoffToRemote()
    }

    override fun flow(): Flow<Location> = location.filterNotNull()

    override fun stop() = localSource.stopTracking()

    override fun currentLocation() = location.value

    private suspend fun flowToCurrentSession() =
        localSource.getLocationsFlow().map(::save).filterNotNull().onEach(::update).collect()

    private fun save(latLon: LatLng): Location? {
        return location.updateAndGet { loc ->
            loc?.copy(point = loc.point.copy(latLng = latLon))
        }
    }

    private fun update(location: Location) {
        currentSession.update(location)
    }

    private suspend fun exponentialBackoffToRemote() = Unit
}
