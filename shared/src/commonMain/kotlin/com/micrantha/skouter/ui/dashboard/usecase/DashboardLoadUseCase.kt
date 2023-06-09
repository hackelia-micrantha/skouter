package com.micrantha.skouter.ui.dashboard.usecase

import com.micrantha.bluebell.domain.usecase.flowUseCase
import com.micrantha.skouter.data.account.model.CurrentSession
import com.micrantha.skouter.domain.model.Location.Point
import com.micrantha.skouter.domain.repository.PlayerRepository
import com.micrantha.skouter.domain.repository.ThingRepository
import com.micrantha.skouter.ui.dashboard.DashboardAction.Loaded
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class DashboardLoadUseCase(
    private val thingsRepository: ThingRepository,
    private val playerRepository: PlayerRepository,
    private val currentSession: CurrentSession
) {
    suspend operator fun invoke() = flowUseCase {
        val player = currentSession.requirePlayer()
        val location = player.location?.point

        combine(
            flow = things(location, player.id),
            flow2 = friends,
            flow3 = players
        )
        { things, friends, players ->
            Result.success(Loaded(things, friends, players))
        }
    }


    private fun things(location: Point?, playerID: String) =
        flow {
            val res = if (location != null) {
                thingsRepository.nearby(location = location)
            } else {
                thingsRepository.things(playerID)
            }
            res.onSuccess { emit(it) }
        }

    private val friends by lazy {
        flow {
            playerRepository.players()
                .onSuccess { emit(it) }
        }
    }

    private val players by lazy {
        flow {
            playerRepository.players()
                .onSuccess { emit(it) }
        }
    }
}
