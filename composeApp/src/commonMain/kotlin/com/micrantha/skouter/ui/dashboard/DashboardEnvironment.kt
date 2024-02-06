package com.micrantha.skouter.ui.dashboard

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.domain.ext.busy
import com.micrantha.bluebell.domain.ext.failure
import com.micrantha.bluebell.domain.model.Ready
import com.micrantha.bluebell.domain.model.isReady
import com.micrantha.bluebell.domain.model.map
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.bluebell.ui.screen.navigate
import com.micrantha.skouter.ui.component.S
import com.micrantha.skouter.ui.dashboard.DashboardAction.GuessThing
import com.micrantha.skouter.ui.dashboard.DashboardAction.Load
import com.micrantha.skouter.ui.dashboard.DashboardAction.LoadError
import com.micrantha.skouter.ui.dashboard.DashboardAction.Loaded
import com.micrantha.skouter.ui.dashboard.DashboardAction.ScanNewThing
import com.micrantha.skouter.ui.dashboard.DashboardUiState.Data
import com.micrantha.skouter.ui.dashboard.DashboardUiState.Data.Nearby
import com.micrantha.skouter.ui.dashboard.DashboardUiState.Data.TabContent
import com.micrantha.skouter.ui.dashboard.usecase.DashboardLoadUseCase
import com.micrantha.skouter.ui.game.component.GameAction
import com.micrantha.skouter.ui.game.detail.GameDetailScreenArg
import com.micrantha.skouter.ui.game.detail.GameDetailsScreen
import com.micrantha.skouter.ui.scan.capture.ScanCaptureScreen
import com.micrantha.skouter.ui.scan.guess.ScanGuessScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DashboardEnvironment(
    private val context: ScreenContext,
    private val dashboardLoadUseCase: DashboardLoadUseCase
) : Reducer<DashboardState>, Effect<DashboardState>, Dispatcher by context.dispatcher {

    companion object : StateMapper<DashboardState, DashboardUiState> {
        override fun map(state: DashboardState) = DashboardUiState(
            status = state.status.map {
                Data(
                    nearby = Nearby(
                        players = TabContent(
                            data = state.players ?: emptyList(),
                            hasMore = false
                        ),
                        things = TabContent(
                            state.things ?: emptyList(),
                            hasMore = false
                        ),
                    ),
                    friends = TabContent(
                        data = state.friends ?: emptyList(),
                        hasMore = false
                    )
                )
            }
        )
    }

    override fun reduce(state: DashboardState, action: Action): DashboardState {
        return when (action) {
            is Loaded -> state.copy(
                status = Ready(),
                things = action.things,
                players = action.players,
                friends = action.friends
            )

            is LoadError -> state.copy(
                status = context.i18n.failure(S.NetworkFailure)
            )

            is Load -> if (state.status.isReady) state else state.copy(
                status = context.i18n.busy(S.LoadingDashboard)
            )

            else -> state
        }
    }

    override suspend fun invoke(action: Action, state: DashboardState) {
        when (action) {
            is GameAction.GameClicked -> context.navigate<GameDetailsScreen, GameDetailScreenArg>(
                arg = action.arg
            )

            is Load ->
                dashboardLoadUseCase().onEach { res ->
                    res.onSuccess { dispatch(it) }
                        .onFailure { dispatch(LoadError) }
                }.launchIn(dispatchScope)

            is ScanNewThing -> context.navigate<ScanCaptureScreen>()

            is GuessThing -> context.router.navigate(
                ScanGuessScreen(
                    action.thing.id
                )
            )
        }
    }
}
