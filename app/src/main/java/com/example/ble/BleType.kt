package com.example.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import java.util.*

sealed class BleOperationType {
    abstract val device: BluetoothDevice
}

data class Connect(override val device: BluetoothDevice, val context: Context) : BleOperationType()
data class CharacteristicRead(
    override val device: BluetoothDevice,
    val characteristicUuid: UUID
) : BleOperationType()