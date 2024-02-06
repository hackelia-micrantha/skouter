package com.micrantha.bluebell.domain.history

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Reducer

fun <State> historyReducerOf(reducer: Reducer<State>): Reducer<HistoryState<State>> =
    HistoryReducer(reducer)

class HistoryReducer<State>(private val reducer: Reducer<State>) : Reducer<HistoryState<State>> {

    override fun reduce(state: HistoryState<State>, action: Action): HistoryState<State> {
        return when (action) {
            is HistoryAction.Redo -> jump(state, 1)
            is HistoryAction.Undo -> jump(state, -1)
            is HistoryAction.Jump -> jump(state, action.index)
            else -> fallback(state, action)
        }
    }

    private fun jump(history: HistoryState<State>, index: Int): HistoryState<State> = when {
        (index > 0) -> jumpNext(history, index - 1)
        (index < 0) -> jumpPrev(history, history.prev.size + index - 1)
        else -> history
    }

    private fun jumpNext(history: HistoryState<State>, index: Int): HistoryState<State> {
        if (index < 0 || index >= history.next.size) return history

        val item = history.next[index]

        // put the old current and next until new current into prev
        val prev = history.prev.toMutableList().apply {
            add(history.state)
            addAll(history.next.subList(0, index))
        }

        // put the prev after current into next if any
        val next = history.prev.subList(index + 1, history.prev.size)

        // set new current and history
        return history.copy(
            prev = prev,
            state = item,
            next = next
        )
    }

    private fun jumpPrev(history: HistoryState<State>, index: Int): HistoryState<State> {
        if (index < 0 || index >= history.prev.size) return history

        val item = history.prev[index]

        // set prev to everything until current
        val prev = history.prev.subList(0, index)

        // set prev to every after current and old current
        val next = history.prev.subList(index + 1, history.prev.size).toMutableList().apply {
            add(history.state)
        }

        // set new current and history
        return history.copy(
            prev = prev,
            state = item,
            next = next
        )
    }

    private fun fallback(history: HistoryState<State>, action: Action): HistoryState<State> {
        // reducer per normal
        val it = reducer.reduce(history.state, action)
        if (it == history.state) {
            return history
        }
        // add to prev and clear next
        val prev = history.prev.toMutableList().apply {
            add(it)
        }
        return history.copy(
            prev = prev,
            state = it,
            next = listOf()
        )
    }
}

