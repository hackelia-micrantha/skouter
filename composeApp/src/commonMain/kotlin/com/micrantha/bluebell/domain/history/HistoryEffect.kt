package com.micrantha.bluebell.domain.history

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Effect

fun <State> historyEffectOf(effect: Effect<State>): Effect<HistoryState<State>> =
    object : Effect<HistoryState<State>> {
        override suspend fun invoke(action: Action, state: HistoryState<State>) {
            effect.invoke(action, state.state)
        }
    }
