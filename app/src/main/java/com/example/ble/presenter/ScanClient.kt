package com.example.ble.presenter

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.ble.presenter.domain.ScanState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class ScanClient @Inject constructor() : ScanCallback() {

    private val _state = MutableSharedFlow<ScanState>()
    val state: SharedFlow<ScanState> = _state.asSharedFlow()

    private fun notifyScanState(state: ScanState) {
        _state.tryEmit(state)
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        notifyScanState(ScanState.ScanResult(result = result))
    }

    override fun onScanFailed(errorCode: Int) {
        notifyScanState(ScanState.ScanFailed("onScanFailed: code $errorCode"))
        Log.e("ScanCallback", "onScanFailed: code $errorCode")
    }
}
