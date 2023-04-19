package com.micrantha.bluebell.ui.navi

import androidx.compose.runtime.Composable
import com.micrantha.bluebell.ui.Modules
import com.micrantha.bluebell.ui.err.fail
import com.micrantha.bluebell.ui.view.ViewContext
import com.micrantha.skouter.presentation.view.ViewContextModel
import com.micrantha.skouter.presentation.view.ViewModel
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

interface Route {
    val path: String
    val isDefault: Boolean
}

typealias Screen<T> = @Composable (viewModel: T) -> Unit

typealias RouteRenderer = @Composable (ViewContext) -> Unit

typealias MappedRoutes = Map<Route, RouteRenderer>

class RouteBuilder() {
    var defaultRoute: Route? = null

    private val routedScreens = mutableMapOf<Route, RouteRenderer>()

    internal inline infix fun <State, reified T : ViewContextModel<State>> Route.to(noinline screen: Screen<T>) {
        routedScreens[this] = { context ->
            screen(Modules.get { parametersOf(context) })
        }
    }

    fun build() = NavigationRoutes(
        defaultRoute = defaultRoute ?: routedScreens.keys.find { it.isDefault }
        ?: routedScreens.keys.firstOrNull()
        ?: fail("no default route configured"),
        routedScreens
    )
}

fun routes(builder: RouteBuilder.() -> Unit) =
    RouteBuilder().apply(builder)
