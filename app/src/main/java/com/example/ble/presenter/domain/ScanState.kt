package com.example.ble.presenter.domain

sealed class ScanState {
    data class ScanResult(val result: android.bluetooth.le.ScanResult) : ScanState()
    data class ScanFailed(val message : String) : ScanState()
}