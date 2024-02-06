package com.micrantha.skouter.ui.scan.edit

import com.benasher44.uuid.uuid4
import com.micrantha.bluebell.data.Log
import com.micrantha.bluebell.domain.arch.Action
import com.micrantha.bluebell.domain.arch.Dispatcher
import com.micrantha.bluebell.domain.arch.Effect
import com.micrantha.bluebell.domain.arch.Reducer
import com.micrantha.bluebell.domain.arch.StateMapper
import com.micrantha.bluebell.ui.components.Router
import com.micrantha.bluebell.ui.screen.ScreenContext
import com.micrantha.skouter.domain.model.Clues
import com.micrantha.skouter.ui.component.Choice
import com.micrantha.skouter.ui.component.updateKey
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.ClearColor
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.ClearLabel
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.ColorChanged
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.CustomLabelChanged
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.Init
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.LabelChanged
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.LoadError
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.LoadedImage
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.NameChanged
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.SaveScanEdit
import com.micrantha.skouter.ui.scan.edit.ScanEditAction.SaveThingError
import com.micrantha.skouter.ui.scan.usecase.GetEditCaptureUseCase
import com.micrantha.skouter.ui.scan.usecase.SaveCaptureUseCase

class ScanEditEnvironment(
    private val context: ScreenContext,
    private val saveCaptureUseCase: SaveCaptureUseCase,
    private val getEditCaptureUseCase: GetEditCaptureUseCase,
) : Reducer<ScanEditState>, Effect<ScanEditState>,
    StateMapper<ScanEditState, ScanEditUiState>,
    Dispatcher by context.dispatcher,
    Router by context.router {

    override fun reduce(state: ScanEditState, action: Action) = when (action) {
        is Init -> state.copy(
            labels = action.proof.clues?.labels?.associate {
                uuid4().toString() to it
            }?.toMutableMap(),
            colors = action.proof.clues?.colors?.associate {
                uuid4().toString() to it
            }?.toMutableMap(),
            proof = action.proof
        )

        is LabelChanged -> state.copy(
            customLabel = null,
            labels = state.labels?.updateKey(action.data.key) { clue ->
                clue.copy(data = action.data.tag)
            }
        )

        is ColorChanged -> state.copy(
            colors = state.colors?.updateKey(action.data.key) { clue ->
                clue.copy(data = action.data.tag)
            }
        )

        is NameChanged -> state.copy(
            name = action.data
        )

        is LoadedImage -> state.copy(
            image = action.data
        )

        is ClearLabel -> state.copy(
            customLabel = ""
        )

        is ClearColor -> state.copy(
            customColor = ""
        )

        is CustomLabelChanged -> state.copy(customLabel = action.data)
        else -> state
    }

    override suspend fun invoke(action: Action, state: ScanEditState) {
        when (action) {
            is SaveScanEdit -> saveCaptureUseCase(
                state.asProof()
            ).onSuccess {
                navigateBack()
            }.onFailure {
                Log.e("saving scan", it)
                dispatch(SaveThingError)
            }

            is Init -> getEditCaptureUseCase(action.proof.image)
                .onSuccess {
                    dispatch(LoadedImage(it))
                }.onFailure {
                    Log.e("unable to load image", it)
                    dispatch(LoadError)
                }
        }
    }

    override fun map(state: ScanEditState) = ScanEditUiState(
        labels = state.labels?.map {
            Choice(
                label = it.value.display(),
                tag = it.value.data,
                key = it.key
            )
        } ?: emptyList(),
        customLabel = state.customLabel,
        colors = state.colors?.map {
            Choice(
                label = it.value.display(),
                tag = it.value.data,
                key = it.key
            )
        } ?: emptyList(),
        customColor = state.customColor,
        name = state.name ?: "",
        image = state.image,
        enabled = state.disabled.not()
    )

    companion object {

        private fun ScanEditState.asProof() = proof!!.copy(
            clues = Clues(
                labels = labels?.values?.toSet(),
                colors = colors?.values?.toSet()
            ),
            name = name,
        )
    }

}
