package com.micrantha.skouter.ui.game.details

import com.micrantha.bluebell.data.err.fail
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.ext.busy
import com.micrantha.bluebell.domain.ext.failure
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.domain.model.Ready
import com.micrantha.bluebell.domain.model.UiResult.Ready
import com.micrantha.bluebell.domain.model.mapNotNull
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.bluebell.ui.screen.StateMapper
import com.micrantha.skouter.domain.repository.GameRepository
import com.micrantha.skouter.ui.components.Strings.LoadingGame
import com.micrantha.skouter.ui.components.toi18n
import com.micrantha.skouter.ui.game.action.GameAction.Failure
import com.micrantha.skouter.ui.game.action.GameAction.ImageFailed
import com.micrantha.skouter.ui.game.action.GameAction.LoadedImage
import com.micrantha.skouter.ui.game.details.GameDetailsAction.Load
import com.micrantha.skouter.ui.game.details.GameDetailsAction.Loaded
import com.micrantha.skouter.ui.thing.ThingAction.DownloadImage
import com.micrantha.skouter.ui.thing.usecase.LoadImageFromStorageUseCase

class GameDetailsEnvironment(
    private val context: ScreenContext,
    private val gameRepository: GameRepository,
    private val loadImageFromStorageUseCase: LoadImageFromStorageUseCase
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
            images = action.images.toMutableMap()
        )
        is Failure -> state.copy(status = failure(action.error.toi18n()))
        is LoadedImage -> state.apply {
            val ref = images[action.id] ?: fail("no image found for ${action.id}")
            images[action.id] = ref.copy(status = Ready(action.data))
        }
        else -> state
    }

    override suspend fun invoke(action: Action, state: GameDetailsState) {
        when (action) {
            is Load -> {
                gameRepository.game(action.arg.id).onFailure {
                    dispatch(Failure(it))
                }.onSuccess { game ->
                    dispatch(Loaded(game))
                }
            }
            is DownloadImage -> loadImageFromStorageUseCase(action.data)
                .onFailure {
                    dispatch(ImageFailed(action.data.id, it))
                }.onSuccess {
                    dispatch(LoadedImage(action.data.id, it))
                }
        }
    }
}