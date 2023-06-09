package com.micrantha.skouter.ui.dashboard

import com.micrantha.bluebell.data.Log
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.ext.busy
import com.micrantha.bluebell.domain.ext.failure
import com.micrantha.bluebell.domain.model.Ready
import com.micrantha.bluebell.domain.model.map
import com.micrantha.bluebell.ui.components.navigate
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.bluebell.ui.screen.StateMapper
import com.micrantha.skouter.ui.component.S
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
import com.micrantha.skouter.ui.scan.preview.ScanScreen

class DashboardEnvironment(
    private val context: ScreenContext,
    private val dashboardLoadUseCase: DashboardLoadUseCase,
) : Reducer<DashboardState>, Effect<DashboardState>, Dispatcher by context.dispatcher,
    StateMapper<DashboardState, DashboardUiState> {

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
            is Load -> state.copy(
                status = context.i18n.busy(S.LoadingDashboard)
            )
            else -> state
        }
    }

    override suspend fun invoke(action: Action, state: DashboardState) {
        when (action) {
            is GameAction.GameClicked -> context.router.navigate<GameDetailsScreen, GameDetailScreenArg>(
                arg = action.arg
            )
            is ScanNewThing -> context.router.navigate<ScanScreen>()
            is Load -> dashboardLoadUseCase().collect { result ->
                result.onSuccess { dispatch(it) }.onFailure {
                    Log.e("unable to load dashboard", it)
                }
            }
        }
    }
}
