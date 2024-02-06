package com.micrantha.skouter

import com.micrantha.bluebell.platform.ConnectivityStatus
import com.micrantha.bluebell.platform.Platform
import com.micrantha.skouter.data.account.AccountDataRepository
import com.micrantha.skouter.data.clue.ColorDataRepository
import com.micrantha.skouter.data.clue.DetectDataRepository
import com.micrantha.skouter.data.clue.LabelDataRepository
import com.micrantha.skouter.data.clue.MatchDataRepository
import com.micrantha.skouter.data.clue.SegmentDataRepository
import com.micrantha.skouter.data.game.GameDataRepository
import com.micrantha.skouter.data.player.PlayerDataRepository
import com.micrantha.skouter.data.storage.StorageDataRepository
import com.micrantha.skouter.data.system.LocationDataRepository
import com.micrantha.skouter.data.system.RealtimeDataRepository
import com.micrantha.skouter.data.thing.ThingDataRepository
import com.micrantha.skouter.domain.repository.AccountRepository
import com.micrantha.skouter.domain.repository.ColorRepository
import com.micrantha.skouter.domain.repository.DetectRepository
import com.micrantha.skouter.domain.repository.GameRepository
import com.micrantha.skouter.domain.repository.LabelRepository
import com.micrantha.skouter.domain.repository.LocationRepository
import com.micrantha.skouter.domain.repository.MatchRepository
import com.micrantha.skouter.domain.repository.PlayerRepository
import com.micrantha.skouter.domain.repository.RealtimeRepository
import com.micrantha.skouter.domain.repository.SegmentRepository
import com.micrantha.skouter.domain.repository.StorageRepository
import com.micrantha.skouter.domain.repository.ThingRepository
import com.micrantha.skouter.platform.scan.analyzer.ColorCaptureAnalyzer
import com.micrantha.skouter.platform.scan.analyzer.DetectCaptureAnalyzer
import com.micrantha.skouter.platform.scan.analyzer.LabelCaptureAnalyzer
import com.micrantha.skouter.platform.scan.analyzer.MatchCaptureAnalyzer
import com.micrantha.skouter.platform.scan.analyzer.SegmentCaptureAnalyzer
import org.kodein.di.DI
import org.kodein.di.bindProviderOf
import org.kodein.di.bindSingleton
import org.kodein.di.delegate

fun iosModules(app: UIApplicationController) = DI {

    bindSingleton { Platform(app) }

    bindProviderOf(::LabelCaptureAnalyzer)

    bindProviderOf(::ColorCaptureAnalyzer)

    bindProviderOf(::DetectCaptureAnalyzer)

    bindProviderOf(::SegmentCaptureAnalyzer)

    bindProviderOf(::MatchCaptureAnalyzer)

    delegate<AccountRepository>().to<AccountDataRepository>()
    delegate<ThingRepository>().to<ThingDataRepository>()
    delegate<LabelRepository>().to<LabelDataRepository>()
    delegate<ColorRepository>().to<ColorDataRepository>()
    delegate<MatchRepository>().to<MatchDataRepository>()
    delegate<DetectRepository>().to<DetectDataRepository>()
    delegate<SegmentRepository>().to<SegmentDataRepository>()
    delegate<GameRepository>().to<GameDataRepository>()
    delegate<LocationRepository>().to<LocationDataRepository>()
    delegate<PlayerRepository>().to<PlayerDataRepository>()
    delegate<RealtimeRepository>().to<RealtimeDataRepository>()
    delegate<StorageRepository>().to<StorageDataRepository>()
}
