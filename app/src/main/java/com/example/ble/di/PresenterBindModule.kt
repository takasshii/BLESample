package com.example.ble.di

import com.example.ble.presenter.BlePresenter
import com.example.ble.presenter.BlePresenterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PresenterBindModule {
    @Singleton
    @Binds
    abstract fun bindBlePresenter(
        impl: BlePresenterImpl
    ): BlePresenter
}
