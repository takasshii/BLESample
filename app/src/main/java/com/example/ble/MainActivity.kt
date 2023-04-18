package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.presenter.GattClient
import com.example.ble.ui.BleViewModel
import com.example.ble.util.Constants.ENABLE_BLUETOOTH_REQUEST_CODE
import com.example.ble.util.Constants.RUNTIME_PERMISSION_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private val viewModel: BleViewModel by viewModels()

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    @Inject
    lateinit var gattClient: GattClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.start_scan)
        val stopButton = findViewById<Button>(R.id.stop_scan)
        val writeButton = findViewById<Button>(R.id.write_button)
        val readButton = findViewById<Button>(R.id.read_button)

        val redLedOnButton = findViewById<Button>(R.id.led_red_on)
        val redLedOffButton = findViewById<Button>(R.id.led_red_off)
        val yellowLedOnButton = findViewById<Button>(R.id.led_yellow_on)
        val yellowLedOffButton = findViewById<Button>(R.id.led_yellow_off)
        val whiteLedOnButton = findViewById<Button>(R.id.led_white_on)
        val whiteLedOffButton = findViewById<Button>(R.id.led_white_off)

        startButton.setOnClickListener {
            startBleScan()
        }

        stopButton.setOnClickListener {
            viewModel.clearScanResult(isScan = false)
        }

        writeButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A7".toByteArray(Charsets.UTF_8)
            )
        }

        redLedOnButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A0".toByteArray(Charsets.UTF_8)
            )
        }

        redLedOffButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A1".toByteArray(Charsets.UTF_8)
            )
        }

        yellowLedOnButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A2".toByteArray(Charsets.UTF_8)
            )
        }

        yellowLedOffButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A3".toByteArray(Charsets.UTF_8)
            )
        }

        whiteLedOnButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A4".toByteArray(Charsets.UTF_8)
            )
        }

        whiteLedOffButton.setOnClickListener {
            viewModel.writeCharacteristic(
                gattClient.getWriteCharacteristic(),
                "C0A5".toByteArray(Charsets.UTF_8)
            )
        }

        readButton.setOnClickListener {
            viewModel.readBatteryLevel()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bleState.collect {
                    Log.d("debug", "collect in mainActivity is $it")
                    findViewById<TextView>(R.id.textView).text = it.stateMessage
                    findViewById<TextView>(R.id.result).text = it.result
                    findViewById<TextView>(R.id.result).text = it.errorMessage
                    it.scanResult?.let { scanResult ->
                        viewModel.reflectScanResult(scanResult)
                    }
                }
            }
        }

        //RecyclerViewの取得
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = viewModel.scanResultAdapter

        //LayoutManagerの設定
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()

        // Bluetoothがオンになっているかを確認する
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    // Bluetoothがオフならば設定画面に飛ばす
    // TODO 正しい実装を後で確認する
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    // Bluetoothの設定がOFFならば設定画面を表示するようにする
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RUNTIME_PERMISSION_REQUEST_CODE -> {
                val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
                    it.second == PackageManager.PERMISSION_DENIED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, it.first)
                }
                val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                when {
                    containsPermanentDenial -> {
                        // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
                        // Note: The user will need to navigate to App Settings and manually grant
                        // permissions that were permanently denied
                    }
                    containsDenial -> {
                        requestRelevantRuntimePermissions()
                    }
                    allGranted && hasRequiredRuntimePermissions() -> {
                        startBleScan()
                    }
                    else -> {
                        // Unexpected scenario encountered when handling permissions
                        recreate()
                    }
                }
            }
        }
    }

    // 権限が揃っているかどうかを確認して権限を付与するように促す
    private fun startBleScan() {
        if (!hasRequiredRuntimePermissions()) {
            requestRelevantRuntimePermissions()
        } else {
            viewModel.clearScanResult(isScan = true)
        }
    }

    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) {
            return
        }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Location permission required")
            builder.setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
            )
            builder.setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
            builder.show()
        }
    }

    private fun requestBluetoothPermissions() {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bluetooth permissions required")
            builder.setMessage(
                "Starting from Android 12, the system requires apps to be granted " +
                        "Bluetooth access in order to scan for and connect to BLE devices."
            )
            builder.setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
            builder.show()
        }
    }
}
