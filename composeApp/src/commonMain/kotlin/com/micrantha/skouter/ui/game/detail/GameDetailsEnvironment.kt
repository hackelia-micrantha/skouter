package com.micrantha.skouter.ui.game.detail

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.domain.ext.busy
import com.micrantha.bluebell.domain.ext.failure
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.domain.model.Ready
import com.micrantha.bluebell.domain.model.mapNotNull
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.skouter.domain.repository.GameRepository
import com.micrantha.skouter.ui.component.Strings.LoadingGame
import com.micrantha.skouter.ui.component.toi18n
import com.micrantha.skouter.ui.game.component.GameAction.Failure
import com.micrantha.skouter.ui.game.detail.GameDetailsAction.Load
import com.micrantha.skouter.ui.game.detail.GameDetailsAction.Loaded

class GameDetailsEnvironment(
    private val context: ScreenContext,
    private val gameRepository: GameRepository,
) : Reducer<GameDetailsState>, Effect<GameDetailsState>,
    StateMapper<GameDetailsState, GameDetailsUiState>,
    Dispatcher by context.dispatcher,
    LocalizedRepository by context.i18n {

    override fun map(state: GameDetailsState) = GameDetailsUiState(
        status = state.status.mapNotNull { state.game }
    )

    override fun reduce(state: GameDetailsState, action: Action) = when (action) {
        is Load -> state.copy(status = busy(LoadingGame))
        is Loaded -> state.copy(
            status = Ready(),
            game = action.game,
        )

        is Failure -> state.copy(status = failure(action.error.toi18n()))
        else -> state
    }

    override suspend fun invoke(action: Action, state: GameDetailsState) {
        when (action) {
            is Load -> {
                gameRepository.game(action.id).onFailure {
                    dispatch(Failure(it))
                }.onSuccess { game ->
                    dispatch(Loaded(game))
                }
            }
        }
    }
}
