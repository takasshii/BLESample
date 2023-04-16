package com.example.ble.presenter

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

interface BlePresenter {
    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray)
    fun enableNotifications(characteristic: BluetoothGattCharacteristic)
    fun disableNotifications(characteristic: BluetoothGattCharacteristic)
}