package com.micrantha.skouter.ui.dashboard

import com.micrantha.bluebell.domain.model.UiResult
import com.micrantha.skouter.domain.models.GameListing
import com.micrantha.skouter.domain.models.PlayerListing
import com.micrantha.skouter.domain.models.ThingListing

data class Dashboard(
    val games: List<GameListing>,
    val players: List<PlayerListing>,
    val things: List<ThingListing>
)

data class DashboardState(
    val status: UiResult<Dashboard> = UiResult.Default
)