package com.example.ble.domain

import android.bluetooth.le.ScanResult

data class BleUIState(
    val stateMessage: String,
    val scanResult : ScanResult?,
    val result : String,
    val errorMessage : String,
) {
    companion object {
        val INITIAL = BleUIState(
            stateMessage = "",
            scanResult = null,
            result = "",
            errorMessage = ""
        )
    }
}