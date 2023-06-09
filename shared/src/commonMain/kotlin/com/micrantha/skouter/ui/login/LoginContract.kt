package com.micrantha.skouter.ui.login

import Skouter.shared.BuildConfig
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.model.UiResult

data class LoginState(
    val email: String = BuildConfig.userLoginEmail,
    val password: String = BuildConfig.userLoginPassword,
    val status: UiResult<Unit> = UiResult.Default
)

data class LoginUiState(
    val email: String,
    val password: String,
    val status: UiResult<Unit>
)


sealed class LoginAction : Action {

    object OnLogin : LoginAction()

    data class OnError(val err: Throwable) : LoginAction()

    object OnSuccess : LoginAction()

    data class ChangedPassword(val password: String) : LoginAction()

    data class ChangedEmail(val email: String) : LoginAction()
}
