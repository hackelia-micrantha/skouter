package com.micrantha.bluebell.domain.arch

import kotlinx.coroutines.flow.StateFlow

interface Stateful<State> {
    val state: StateFlow<State>
}
