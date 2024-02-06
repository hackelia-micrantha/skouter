package com.micrantha.skouter.ui.game.list

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.domain.ext.busy
import com.micrantha.bluebell.domain.ext.failure
import com.micrantha.bluebell.domain.ext.status
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.ui.components.Router
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.bluebell.ui.screen.navigate
import com.micrantha.skouter.domain.repository.GameRepository
import com.micrantha.skouter.ui.component.Strings.LoadingGames
import com.micrantha.skouter.ui.component.toi18n
import com.micrantha.skouter.ui.game.component.GameAction.Failure
import com.micrantha.skouter.ui.game.component.GameAction.GameClicked
import com.micrantha.skouter.ui.game.create.GameCreateScreen
import com.micrantha.skouter.ui.game.detail.GameDetailScreenArg
import com.micrantha.skouter.ui.game.detail.GameDetailsScreen
import com.micrantha.skouter.ui.game.list.GameListAction.Load
import com.micrantha.skouter.ui.game.list.GameListAction.Loaded
import com.micrantha.skouter.ui.game.list.GameListAction.NewGame

class GameListEnvironment(
    private val context: ScreenContext,
    private val repository: GameRepository,
) : Reducer<GameListState>, Effect<GameListState>,
    StateMapper<GameListState, GameListUiState>,
    LocalizedRepository by context.i18n, Router by context.router,
    Dispatcher by context.dispatcher {

    override fun map(state: GameListState) = GameListUiState(
        status = state.status
    )

    override fun reduce(state: GameListState, action: Action) = when (action) {
        is Load ->
            state.copy(status = busy(LoadingGames))

        is Loaded -> state.copy(
            status = action.data.status()
        )

        is Failure -> state.copy(status = failure(action.error.toi18n()))
        else -> state
    }

    override suspend fun invoke(action: Action, state: GameListState) {
        when (action) {
            is Load -> repository.games()
                .onFailure { dispatch(Failure(it)) }
                .onSuccess { dispatch(Loaded(it)) }

            is NewGame -> context.navigate<GameCreateScreen>()
            is GameClicked -> context.navigate<GameDetailsScreen, GameDetailScreenArg>(arg = action.arg)
        }
    }
}
