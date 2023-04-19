package com.micrantha.bluebell.ui.flux

import com.micrantha.bluebell.ui.arch.Action
import com.micrantha.bluebell.ui.arch.Dispatcher
import com.micrantha.bluebell.ui.arch.Effect
import com.micrantha.bluebell.ui.arch.Reducer
import com.micrantha.bluebell.ui.arch.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class FluxStore<State> internal constructor(
    initialState: State,
    private val stateScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined) + Job()
) : Store<State>, Dispatcher, Store.Listener<State> {
    private val reducers = mutableListOf<Reducer<State>>()
    private val effects = mutableListOf<Effect<State>>()
    private val current = MutableStateFlow(initialState)

    override fun dispatch(action: Action) {
        current.update { state ->
            reducers.fold(state) { next, reducer -> reducer(next, action) }
        }
        stateScope.launch {
            effects.forEach { it(action, current.value) }
        }
    }

    override fun addReducer(reducer: Reducer<State>): FluxStore<State> {
        reducers.add(reducer)
        return this
    }

    override fun applyEffect(effect: Effect<State>): FluxStore<State> {
        effects.add(effect)
        return this
    }

    override fun state(): StateFlow<State> = current.asStateFlow()
    
    override fun listen(block: (State) -> Unit): Store<State> {
        current.onEach { block(it) }.launchIn(stateScope)
        return this
    }
}

