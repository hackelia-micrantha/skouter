package com.micrantha.skouter.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import com.micrantha.bluebell.domain.arch.Dispatch
import com.micrantha.bluebell.domain.ext.enabled
import com.micrantha.bluebell.domain.i18n.stringResource
import com.micrantha.bluebell.domain.model.UiResult.Default
import com.micrantha.bluebell.ui.theme.Dimensions
import com.micrantha.skouter.ui.MainAction.Load
import com.micrantha.skouter.ui.Skouter
import com.micrantha.skouter.ui.components.Strings

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<LoginScreenModel>()

        val state by viewModel.state().collectAsState()

        LaunchedEffect(viewModel) {
            viewModel.dispatch(Load)
        }

        render(state, viewModel::dispatch)
    }

    @Composable
    fun render(state: LoginUiState, dispatch: Dispatch) {
        when (state.status) {
            is Default -> SplashScreen()
            else -> LoginForm(state, dispatch)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LoginForm(state: LoginUiState, dispatch: Dispatch) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Skouter.defaultIcon,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.List.placeholder),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(Strings.AppTitle),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = Dimensions.Text.Large)
                )

                Spacer(modifier = Modifier.heightIn(Dimensions.screen))

                TextField(
                    modifier = Modifier.padding(Dimensions.content),
                    value = state.email,
                    enabled = state.status.enabled(),
                    maxLines = 1,
                    label = { Text(stringResource(Strings.Email)) },
                    onValueChange = { dispatch(LoginAction.ChangedEmail(it)) },
                    placeholder = { Text(stringResource(Strings.LoginEmailPlaceholder)) }
                )
                TextField(
                    enabled = state.status.enabled(),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(Dimensions.content),
                    maxLines = 1,
                    label = { Text(stringResource(Strings.Password)) },
                    value = state.password,
                    onValueChange = { dispatch(LoginAction.ChangedPassword(it)) },
                    placeholder = { Text(stringResource(Strings.LoginPasswordPlaceholder)) }
                )

                OutlinedButton(
                    enabled = state.status.enabled(),
                    modifier = Modifier.fillMaxWidth().padding(Dimensions.screen),
                    contentPadding = PaddingValues(Dimensions.content),
                    onClick = { dispatch(LoginAction.OnLogin) }) {
                    Text(stringResource(Strings.Login))
                }
            }
        }
    }

    @Composable
    private fun SplashScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Skouter.defaultIcon,
                modifier = Modifier.size(Dimensions.List.placeholder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

}
