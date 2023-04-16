package com.example.ble.presenter.domain

sealed class GattState {
    data class GattConnect(val message : String) : GattState()
    data class GattDisconnect(val message : String) : GattState()
    data class GattError(val message: String) : GattState()

    data class ServiceDiscovered(val message: String) : GattState()

    data class CharacteristicWriteSuccess(val message: String) : GattState()
    data class CharacteristicWriteError(val message: String) : GattState()


    data class CharacteristicReadSuccess(val message: String) : GattState()
    data class CharacteristicReadError(val message: String) : GattState()
}