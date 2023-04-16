
package com.example.ble.domain

import android.bluetooth.le.ScanResult
import com.example.ble.BuildConfig
import com.example.ble.presenter.BlePresenter
import com.example.ble.presenter.GattClient
import com.example.ble.presenter.ScanClient
import com.example.ble.presenter.domain.GattState
import com.example.ble.presenter.domain.ScanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.*
import javax.inject.Inject

class BleUseCase @Inject constructor(
    private val gattClient: GattClient,
    private val scanClient: ScanClient,
    private val blePresenter: BlePresenter
) {
    operator fun invoke(): Flow<BleUIState> {
        return combine(
            gattClient.state,
            scanClient.state
        ) { gattState, scanState ->

            val characteristic = gattClient.getBluetoothGatt()?.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                ?.getCharacteristic(UUID.fromString(BuildConfig.CHARACTERISTIC_UUID))

            var stateMessage = ""
            var result = ""
            var errorMessage = ""
            var scanResult : ScanResult? = null

            when (gattState) {
                is GattState.GattConnect -> {
                    stateMessage = gattState.message
                }
                is GattState.GattDisconnect -> {
                    characteristic?.let {
                        blePresenter.disableNotifications(it)
                    }
                    stateMessage = gattState.message
                }
                is GattState.ServiceDiscovered -> {
                    characteristic?.let {
                        blePresenter.enableNotifications(it)
                    }
                    stateMessage = gattState.message
                }
                is GattState.CharacteristicWriteSuccess -> {
                    result = gattState.message
                }
                is GattState.CharacteristicReadSuccess -> {
                    result = gattState.message
                }
                is GattState.CharacteristicWriteError -> {
                    errorMessage = gattState.message
                }
                is GattState.CharacteristicReadError -> {
                    errorMessage = gattState.message
                }
                is GattState.GattError -> {
                    errorMessage = gattState.message
                }
            }

            when (scanState) {
                is ScanState.ScanResult -> {
                    scanResult = scanState.result
                }
                is ScanState.ScanFailed -> {
                    errorMessage = scanState.message
                }
            }

            BleUIState(
                stateMessage = stateMessage,
                result = result,
                errorMessage = errorMessage,
                scanResult = scanResult
            )
        }
    }
}