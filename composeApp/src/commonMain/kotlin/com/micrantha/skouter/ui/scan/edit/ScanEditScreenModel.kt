package com.micrantha.skouter.ui.scan.edit

import com.micrantha.bluebell.domain.history.HistoryState
import com.micrantha.bluebell.domain.history.historyEffectOf
import com.micrantha.bluebell.domain.history.historyMapperOf
import com.micrantha.bluebell.domain.history.historyReducerOf
import com.micrantha.bluebell.domain.history.historyStateOf
import com.micrantha.bluebell.ui.screen.MappedScreenModel
import com.micrantha.bluebell.ui.screen.ScreenContext

class ScanEditScreenModel(
    context: ScreenContext,
    environment: ScanEditEnvironment
) : MappedScreenModel<HistoryState<ScanEditState>, ScanEditUiState>(
    context,
    historyStateOf(ScanEditState()),
    historyMapperOf(environment)
) {
    init {
        store.addReducer(historyReducerOf(environment))
            .applyEffect(historyEffectOf(environment))
    }
}
