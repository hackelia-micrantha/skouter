package com.micrantha.skouter.ui.login

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.micrantha.bluebell.Platform
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.arch.Store
import com.micrantha.bluebell.domain.arch.StoreFactory
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.domain.model.ResultStatus
import com.micrantha.bluebell.ui.navi.LocalRouter
import com.micrantha.bluebell.ui.navi.Route
import com.micrantha.bluebell.ui.navi.RouteContext
import com.micrantha.bluebell.ui.navi.RouteRenderer
import com.micrantha.bluebell.ui.navi.Router
import com.micrantha.bluebell.ui.view.LocalViewContext
import com.micrantha.bluebell.ui.view.ViewContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewRouter : Router {
    override fun navigateBack() = Unit

    override fun navigate(route: Route) = Unit

    override fun changeNavigationContext(context: RouteContext) = Unit

    override fun get(route: Route): RouteRenderer? = null
}

class PreviewContext(
    context: Context,
    val platform: Platform = Platform(context),
    val router: PreviewRouter = PreviewRouter()
) : StoreFactory,
    Dispatcher, LocalizedRepository by platform, Router by router, ViewContext {
    override fun dispatch(action: Action) {}

    override fun <T> createStore(with: T): Store<T> {
        return object : Store<T> {
            override fun state(): StateFlow<T> = MutableStateFlow(with)

            override fun applyEffect(effect: Effect<T>): Store<T> = this

            override fun addReducer(reducer: Reducer<T>): Store<T> = this
        }
    }
}

@Composable
fun PreviewContext(content: @Composable () -> Unit) {

    CompositionLocalProvider(
        LocalRouter provides PreviewRouter(),
        LocalViewContext provides PreviewContext(LocalContext.current)
    ) {
        content()
    }
}

@Preview
@Composable
fun LoginPreview() = PreviewContext {
    LoginContent(
        state = LoginUiState(
            email = "",
            password = "",
            isLoggedIn = false,
            status = ResultStatus.Default
        )
    ) {}
}