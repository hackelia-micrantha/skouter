package com.micrantha.skouter.ui.login

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.domain.ext.busy
import com.micrantha.bluebell.domain.ext.failure
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.domain.model.UiResult.Default
import com.micrantha.bluebell.ui.components.Router
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.bluebell.ui.screen.navigate
import com.micrantha.skouter.domain.repository.AccountRepository
import com.micrantha.skouter.ui.component.Strings
import com.micrantha.skouter.ui.dashboard.DashboardScreen
import com.micrantha.skouter.ui.login.LoginAction.ChangedEmail
import com.micrantha.skouter.ui.login.LoginAction.ChangedPassword
import com.micrantha.skouter.ui.login.LoginAction.OnError
import com.micrantha.skouter.ui.login.LoginAction.OnLogin
import com.micrantha.skouter.ui.login.LoginAction.OnSuccess
import com.micrantha.skouter.ui.login.LoginAction.ResetStatus

class LoginEnvironment(
    private val context: ScreenContext,
    private val accountRepository: AccountRepository,
) : Reducer<LoginState>, Effect<LoginState>,
    Dispatcher by context.dispatcher,
    LocalizedRepository by context.i18n, Router by context.router {

    companion object : StateMapper<LoginState, LoginUiState> {
        override fun map(state: LoginState) = LoginUiState(
            email = state.email,
            password = state.hash,
            status = state.status
        )
    }

    override fun reduce(state: LoginState, action: Action) = when (action) {
        is ChangedEmail -> state.copy(email = action.email)
        is ChangedPassword -> state.copy(hash = action.password)
        is OnSuccess -> state.copy(status = Default)
        is OnLogin -> state.copy(status = busy(Strings.LoggingIn))
        is OnError -> state.copy(status = failure(Strings.LoginFailed))
        is ResetStatus -> state.copy(status = Default)
        else -> state
    }

    override suspend fun invoke(action: Action, state: LoginState) {
        when (action) {

            is OnLogin -> accountRepository.login(state.email, state.hash)
                .onFailure {
                    dispatch(OnError(it))
                }.onSuccess {
                    dispatch(OnSuccess)
                }

            is OnSuccess -> context.navigate<DashboardScreen>()
        }
    }
}

