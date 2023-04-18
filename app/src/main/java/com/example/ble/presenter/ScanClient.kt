package com.example.ble.presenter

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.ble.presenter.domain.ScanState
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanClient @Inject constructor() : ScanCallback() {

    private val _state = MutableStateFlow<ScanState>(ScanState.ScanFailed(""))
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private fun notifyScanState(state: ScanState) {
        _state.value = state
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        notifyScanState(ScanState.ScanResult(result = result))
    }

    override fun onScanFailed(errorCode: Int) {
        notifyScanState(ScanState.ScanFailed("onScanFailed: code $errorCode"))
        Log.e("ScanCallback", "onScanFailed: code $errorCode")
    }
}
