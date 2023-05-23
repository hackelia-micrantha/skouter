package com.micrantha.skouter.ui.game.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import com.micrantha.bluebell.domain.arch.Dispatch
import com.micrantha.bluebell.domain.i18n.longDateTime
import com.micrantha.bluebell.domain.i18n.stringResource
import com.micrantha.bluebell.domain.model.UiResult
import com.micrantha.bluebell.ui.components.LabeledText
import com.micrantha.bluebell.ui.components.TabPager
import com.micrantha.bluebell.ui.components.status.FailureContent
import com.micrantha.bluebell.ui.components.status.LoadingContent
import com.micrantha.bluebell.ui.scaffold.Scaffolding
import com.micrantha.bluebell.ui.theme.Dimensions
import com.micrantha.skouter.domain.models.Game
import com.micrantha.skouter.ui.components.Strings
import com.micrantha.skouter.ui.player.components.PlayerListContent
import com.micrantha.skouter.ui.thing.components.ThingListContent

data class GameDetailsScreen(private val arg: GameDetailScreenArg) : Screen, Scaffolding {

    @Composable
    override fun title() = arg.title

    @Composable
    override fun Content() {
        val viewModel: GameDetailsScreenModel = rememberScreenModel(arg = arg)

        val state by viewModel.state().collectAsState()

        Render(state, viewModel::dispatch)
    }

    @Composable
    private fun Render(state: GameDetailsUiState, dispatch: Dispatch) {
        when (val status = state.status) {
            is UiResult.Busy -> LoadingContent(status.message)
            is UiResult.Failure -> FailureContent(status.message)
            is UiResult.Ready -> GameDetailsContent(status.data, dispatch)
            else -> Unit
        }
    }

    @Composable
    private fun GameDetailsContent(game: Game, dispatch: Dispatch) {

        Column(
            modifier = Modifier.fillMaxSize().padding(Dimensions.screen)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimensions.content)) {
                    LabeledText(
                        label = stringResource(Strings.CreatedAt),
                        text = longDateTime(game.createdAt),
                    )

                    LabeledText(
                        label = stringResource(Strings.ExpiresAt),
                        text = longDateTime(game.expires),
                    )

                    LabeledText(
                        label = stringResource(Strings.NextTurn),
                        text = game.turnDuration.toString(),
                    )
                }
            }

            TabPager(
                stringResource(Strings.Things),
                stringResource(Strings.Players),
                stringResource(Strings.Location)
            ) { index, _ ->
                when (index) {
                    0 -> ThingListContent(game.things, dispatch)
                    1 -> PlayerListContent(game.players, dispatch)
                    2 -> GameLocationContent()
                }
            }
        }
    }

    @Composable
    private fun GameLocationContent() {

    }
}
