package com.micrantha.skouter.ui.login

import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.model.UiResult
import com.micrantha.skouter.SkouterConfig

data class LoginState(
    val email: String = SkouterConfig.userLoginEmail,
    val hash: String = SkouterConfig.userLoginPassword,
    val status: UiResult<Unit> = UiResult.Default
)

data class LoginUiState(
    val email: String,
    val password: String,
    val status: UiResult<Unit>
)


sealed interface LoginAction : Action {

    data object OnLogin : LoginAction

    data class OnError(val err: Throwable) : LoginAction

    data object OnSuccess : LoginAction

    data class ChangedPassword(val password: String) : LoginAction

    data class ChangedEmail(val email: String) : LoginAction

    data object ResetStatus: LoginAction
}