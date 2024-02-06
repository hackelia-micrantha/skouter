package com.micrantha.skouter.ui.thing.detail

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.ui.screen.ScreenEnvironment

class ThingDetailEnvironment : ScreenEnvironment<ThingDetailState> {
    override fun reduce(state: ThingDetailState, action: Action) = when (action) {
        else -> state
    }

    override suspend fun invoke(action: Action, state: ThingDetailState) {
        when (action) {
        }
    }

    companion object : StateMapper<ThingDetailState, ThingDetailUiState> {

        override fun map(state: ThingDetailState) = ThingDetailUiState(
            status = state.status
        )
    }
}
