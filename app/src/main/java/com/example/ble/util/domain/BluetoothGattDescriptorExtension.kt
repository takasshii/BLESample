package com.example.ble

import android.bluetooth.BluetoothGattDescriptor

fun BluetoothGattDescriptor.isReadable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_READ) ||
            containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) ||
            containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM)

fun BluetoothGattDescriptor.isWritable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE) ||
            containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) ||
            containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) ||
            containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) ||
            containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM)

fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
    permissions and permission != 0