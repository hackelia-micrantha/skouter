package com.micrantha.bluebell.ui.screen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatch
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.domain.arch.Stateful
import com.micrantha.bluebell.domain.arch.Store
import com.micrantha.bluebell.domain.flux.Flux
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.ui.components.Router
import com.micrantha.bluebell.ui.components.mapIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * contextual view model knows about platform, routing, localization, and global dispatching
 */
abstract class ContextualScreenModel(
    protected val context: ScreenContext
) : ScreenModel, Dispatcher by context.dispatcher, Dispatch {
    protected val router: Router = context.router
    protected val i18n: LocalizedRepository = context.i18n
    override val dispatchScope: CoroutineScope = screenModelScope
}

/**
 * View model with a store that bypasses global dispatcher with local store dispatch by default.
 */
abstract class FluxScreenModel<State>(
    context: ScreenContext,
    initialState: State
) : ContextualScreenModel(context), DIAware by context {
    protected val store: Store<State> by screenModelStore(initialState, screenModelScope)

    // do not use global dispatch by default
    override fun dispatch(action: Action) = store.dispatch(action)
}

private fun <State> FluxScreenModel<State>.screenModelStore(
    initialState: State,
    scope: CoroutineScope
): Lazy<Store<State>> {
    val flux by instance<Flux>()
    return lazy { flux.createStore(initialState, scope) }
}

/**
 * Stateful view model with a store
 */
abstract class StatefulScreenModel<State>(
    context: ScreenContext,
    initialState: State
) : FluxScreenModel<State>(context, initialState), Stateful<State> {
    override val state: StateFlow<State> = store.state
}


/**
 * Stateful view model that maps between internal state and ui state.
 */
abstract class MappedScreenModel<State, UiState>(
    context: ScreenContext,
    initialState: State,
    private val mapper: StateMapper<State, UiState>
) : FluxScreenModel<State>(context, initialState), Stateful<UiState> {
    override val state: StateFlow<UiState> = store.state.mapIn(screenModelScope, mapper::map)
}
