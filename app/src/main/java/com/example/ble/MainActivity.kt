package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.Constants.ENABLE_BLUETOOTH_REQUEST_CODE
import com.example.ble.Constants.RUNTIME_PERMISSION_REQUEST_CODE
import java.util.*

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: BLEListAdapter by lazy {
        BLEListAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                connectGatt(applicationContext, false, gattCallback)
            }
        }
    }

    private var mWriteCharacteristic: BluetoothGattCharacteristic? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

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
            stopBleScan()
        }

        writeButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A7".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        redLedOnButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A0".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        redLedOffButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A1".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        yellowLedOnButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A2".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        yellowLedOffButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A3".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        whiteLedOnButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A4".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        whiteLedOffButton.setOnClickListener {
            // 書き込むデータをバイト配列に変換してペイロードに設定
            val payload = "C0A5".toByteArray(Charsets.UTF_8)

            // BluetoothGattCharacteristicオブジェクトを作成
            mWriteCharacteristic?.let { characteristic ->
                characteristic.value = payload
                writeCharacteristic(characteristic, payload)
            } ?: run {
                Log.e("debug", "writeCharacteristic: Failed to get write characteristic")
            }
        }

        readButton.setOnClickListener {
            readBatteryLevel()
        }

        //RecyclerViewの取得
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = scanResultAdapter

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

    private var isScanning = false

    // 権限が揃っているかどうかを確認して権限を付与するように促す
    private fun startBleScan() {
        if (!hasRequiredRuntimePermissions()) {
            requestRelevantRuntimePermissions()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(mutableListOf(filter), scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
        scanResults.clear()
        scanResultAdapter.notifyDataSetChanged()
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

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    val filter = ScanFilter.Builder().setDeviceAddress(BuildConfig.ESP32_MAC_ADDRESS).build()

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            // device is connected
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // Store a reference to BluetoothGatt
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                        findViewById<TextView>(R.id.textView).text = "Successfully connected"
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                    Handler(Looper.getMainLooper()).post {
                        findViewById<TextView>(R.id.textView).text = "disconnected"
                    }
                    val characteristic = gatt.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                        .getCharacteristic(UUID.fromString(BuildConfig.CHARACTERISTIC_UUID))
                    // onCharacteristicWrite()の受信を無効化
                    // disableNotifications(characteristic)
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
                Handler(Looper.getMainLooper()).post {
                    findViewById<TextView>(R.id.textView).text = "Error $status"
                }
            }
        }

        // gatt's service is discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                val characteristic = gatt.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(BuildConfig.CHARACTERISTIC_UUID))

                val gattServices = gatt.services
                for (gattService in gattServices) {
                    if (gattService.uuid == UUID.fromString(BuildConfig.SERVICE_UUID)) { // YOUR_SERVICE_UUIDには、使用しているサービスのUUIDを指定してください。
                        val characteristics = gattService.characteristics
                        for (characteristic in characteristics) {
                            if (characteristic.uuid == UUID.fromString(BuildConfig.CHARACTERISTIC_UUID)) { // YOUR_CHARACTERISTIC_UUIDには、使用しているキャラクタリスティックのUUIDを指定してください。
                                mWriteCharacteristic = characteristic
                                break
                            }
                        }
                    }
                }
                // onCharacteristicWrite()の受信を有効化
                enableNotifications(characteristic)
                printGattTable() // See implementation just above this section
                // Consider connection setup as complete here
                Handler(Looper.getMainLooper()).post {
                    findViewById<TextView>(R.id.textView).text = "Discovered Service"
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(
                "BluetoothGattCallback",
                "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}"
            )
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Handler(Looper.getMainLooper()).post {
                findViewById<TextView>(R.id.result).text = "call onCharacteristicWrite function"
            }
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Wrote to characteristic $uuid | value: ${value.toHexString()}"
                        )
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text =
                                "wrote ${value.toHexString()}"
                        }
                    }
                    // ATTRIBUTE_LENGTHを超えてしまっている場合
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text =
                                "wrote exceeded connection ATT MTU!"
                        }
                    }
                    // 書き込みが許可されていない場合
                    // チェックする前にfalseを返すことがあるのであまりお勧めされない
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text = "Write not permitted"
                        }
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic write failed for $uuid, error: $status"
                        )
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text = "Characteristic write failed"
                        }
                    }
                }
            }
        }

        // if call readCharacteristic, this method will be called
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n${value.toHexString()}"
                        )
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text = "${this.value}"
                        }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text = "Read not permitted"
                        }
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic read failed for $uuid, error: $status"
                        )
                        Handler(Looper.getMainLooper()).post {
                            findViewById<TextView>(R.id.result).text = "error"
                        }
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                Log.i(
                    "BluetoothGattCallback",
                    "Characteristic $uuid changed | value: ${value.toHexString()}"
                )
            }
        }
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        bluetoothGatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }

    private fun readBatteryLevel() {
        val batteryServiceUuid = UUID.fromString(BuildConfig.SERVICE_UUID)
        val batteryLevelCharUuid = UUID.fromString(BuildConfig.CHARACTERISTIC_UUID)
        bluetoothGatt?.let {
            val batteryLevelChar = it
                .getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
            if (batteryLevelChar?.isReadable() == true) {
                it.readCharacteristic(batteryLevelChar)
            }
        }
    }

    // UUIDとCCCD（通知）のUUIDが一致するかどうかをチェックしている
    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        bluetoothGatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e(
                    "ConnectionManager",
                    "${characteristic.uuid} doesn't support notifications/indications"
                )
                return
            }
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bluetoothGatt?.setCharacteristicNotification(characteristic, true) == false) {
                Log.e(
                    "ConnectionManager",
                    "setCharacteristicNotification failed for ${characteristic.uuid}"
                )
                return
            }
            writeDescriptor(cccDescriptor, payload)
        } ?: Log.e(
            "ConnectionManager",
            "${characteristic.uuid} doesn't contain the CCC descriptor!"
        )
    }

    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e(
                "ConnectionManager",
                "${characteristic.uuid} doesn't support indications/notifications"
            )
            return
        }

        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bluetoothGatt?.setCharacteristicNotification(characteristic, false) == false) {
                Log.e(
                    "ConnectionManager",
                    "setCharacteristicNotification failed for ${characteristic.uuid}"
                )
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: Log.e(
            "ConnectionManager",
            "${characteristic.uuid} doesn't contain the CCC descriptor!"
        )
    }
}