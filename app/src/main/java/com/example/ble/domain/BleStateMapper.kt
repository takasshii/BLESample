package com.example.ble.domain

import android.util.Log
import com.example.ble.BuildConfig
import com.example.ble.presenter.BlePresenter
import com.example.ble.presenter.GattClient
import com.example.ble.presenter.ScanClient
import com.example.ble.presenter.domain.GattState
import com.example.ble.presenter.domain.ScanState
import java.util.*
import javax.inject.Inject

class BleStateMapper @Inject constructor(
    private val gattClient: GattClient,
    private val blePresenter: BlePresenter
) {
    fun map(gattState: GattState, scanState: ScanState) : BleUIState {
        val characteristic =
            gattClient.getBluetoothGatt()?.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                ?.getCharacteristic(UUID.fromString(BuildConfig.CHARACTERISTIC_UUID))

        Log.d("debug", "state is $scanState")

        when (gattState) {
            is GattState.GattDisconnect -> {
                characteristic?.let {
                    blePresenter.disableNotifications(it)
                }
            }
            is GattState.ServiceDiscovered -> {
                Log.d("debug", "サービスが見つかったよ ${gattClient.getBluetoothGatt()}")
                characteristic?.let {
                    blePresenter.enableNotifications(it)
                }
            }
            else -> {}
        }

        val stateMessage = when (gattState) {
            is GattState.GattConnect -> gattState.message
            is GattState.GattDisconnect -> gattState.message
            is GattState.ServiceDiscovered -> gattState.message
            else -> ""
        }

        val result = when (gattState) {
            is GattState.CharacteristicWriteSuccess -> gattState.message
            is GattState.CharacteristicReadSuccess -> gattState.message
            else -> ""
        }

        val gattErrorMessage = when (gattState) {
            is GattState.CharacteristicWriteError -> gattState.message
            is GattState.CharacteristicReadError -> gattState.message
            is GattState.GattError -> gattState.message
            else -> ""
        }

        val scanErrorMessage = when(scanState) {
            is ScanState.ScanFailed -> scanState.message
            else -> ""
        }

        val errorMessage = gattErrorMessage + if (gattErrorMessage.isNotEmpty() && scanErrorMessage.isNotEmpty()) " / " else "" + scanErrorMessage

        val scanResult = if (scanState is ScanState.ScanResult) scanState.result else null

        return BleUIState(
            stateMessage = stateMessage,
            result = result,
            errorMessage = errorMessage,
            scanResult = scanResult
        )
    }
}