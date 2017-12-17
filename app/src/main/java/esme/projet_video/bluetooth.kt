package esme.projet_video

/**
 * Created by darrieu on 10/12/17.
 */

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList
import java.util.UUID


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class bluetooth : AppCompatActivity() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null

    private var mScanning: Boolean = false

    private val mFragmentButton: Button? = null
    private var mScanButton: Button? = null
    private var listViewLE: ListView? = null
    private var listBluetoothDevice: MutableList<BluetoothDevice>? = null
    private var listStringBluetoothDevice: MutableList<String>? = null
    private var adapterLeScanResult: ListAdapter? = null
    private val act2InitReceiver: BroadcastReceiver? = null

    private var mGatt: BluetoothGatt? = null
    private var mProgressDialog: ProgressDialog? = null

    //    private List<BluetoothGattService> mServices;
    private var mComCharacteristic: BluetoothGattCharacteristic? = null

    private var mComValueTextView: TextView? = null
    private val mHandler = Handler()
    private val mHandler2 = Handler()

    private val myIntent: Intent? = null
    //internal
    private var mUIrunnable: Runnable? = object : Runnable {
        override fun run() {
            mComValueTextView!!.text = value.toString()
            //            value += 1;
            //            mComValueTextView.setText(String.valueOf(value));
            if (value === 1) {

                val intent = Intent(applicationContext, MainActivity::class.java)
                //on lance l'intent, cela a pour effet de stoper l'activité courante et lancer une autre activite ici SecondActivite
                startActivity(intent)
                value = 0
            }

           mHandler2.postDelayed(this, 1000)
        }
    }

    // Implementation of onItemClick listener for the adapter (list of devices)
    internal var scanResultOnItemClickListener: AdapterView.OnItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        // Get the bluetooth device according to the position in the list
        //                    final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
        Log.e("onItemClick", "Position = " + position)
        val device = listBluetoothDevice!![position]
        if (mGatt == null) {
            connectToDevice(device)
        }

        /*                    String msg = device.getAddress() + "\n"
                            + getBTDeviceType(device);

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(device.getName())
                            .setMessage(msg)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();*/
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            addBluetoothDevice(result.device)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                addBluetoothDevice(result.device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@bluetooth,
                    "onScanFailed: " + errorCode.toString(),
                    Toast.LENGTH_LONG).show()
        }
    }

    private val gattCallBack = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            //            super.onConnectionStateChange(gatt, status, newState);
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.e("gattCallBack", "STATE CONNECTED")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e("gattCallBack", "STATE DISCONNECTED")
                    mGatt!!.close()
                    mGatt = null
                }
                else -> Log.e("getCallBack", "STATE OTHER")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val mServices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                gatt.services
            } else {
                TODO("VERSION.SDK_INT < JELLY_BEAN_MR2")
            }
            Log.e("bluetooth", "onServicesDiscovered")

            for (i in mServices.indices) {
                Log.e("Service", i.toString() + " : " + mServices[i].uuid.toString())
            }

            for (gattCharacteristic in mServices[2].characteristics) {
                Log.e("Characteristics", gattCharacteristic.uuid.toString())
            }

            //            gatt.readCharacteristic(mServices.get(2).getCharacteristics().get(0));
            mComCharacteristic = mServices[2].characteristics[0]

            setNotifications()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.e("onCharacteristicRead", "")


            val Value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                characteristic.value[0]
            } else {
                TODO("VERSION.SDK_INT < JELLY_BEAN_MR2")
            }

            Log.e("Value = ", (Value!! + 5000).toString())

        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)

            value = characteristic.value[0].toInt()
            Log.e("Value = ", value.toString())




        }

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetooth)

        //        myIntent = new Intent(getApplicationContext(), MapActivity.class);

        // Create the progress dialog and set a custom message
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage("Scanning in progress...")

        // Permission required to launch a BLE scan (popup that ask for permission to have localisation)
        val MY_PERMISSION_REQUEST_CONSTANT = 2
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSION_REQUEST_CONSTANT)

        // Check if BLE is supported on the device.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,
                    "BLUETOOTH LOW ENERGY not supported in this device!",
                    Toast.LENGTH_SHORT).show()
            finish()
        } /*else {
            Toast.makeText(this,
                    "BLUETOOTH_LE is supported in this device!",
                    Toast.LENGTH_SHORT).show();
        } */

        getBluetoothAdapterAndLeScanner()

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported",
                    Toast.LENGTH_SHORT).show()
            finish()
        }

        // Scan button used to detect nearby bluetooth devices
        mScanButton = findViewById<View>(R.id.scan_button) as Button
        mScanButton!!.setOnClickListener { scanLeDevice(true) }


        // Button that enables bluetooth when clicked on
        val mEnableButton = findViewById<View>(R.id.activate_button) as Button
        mEnableButton.setOnClickListener {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, RQS_ENABLE_BLUETOOTH)
            } else {
                Toast.makeText(this@bluetooth, "Bluetooth is already enabled", Toast.LENGTH_LONG).show()
            }
        }


        // Button that stops the scan
        val mStopButton = findViewById<View>(R.id.stop_button) as Button
        mStopButton.setOnClickListener {
            scanLeDevice(false)
            Toast.makeText(this@bluetooth, "Scan stopped", Toast.LENGTH_LONG).show()
            if (mUIrunnable != null && mHandler != null) {
                mHandler2.removeCallbacks(mUIrunnable)
            }
        }

        //Button that disconnects the connection with the device
        val mDisconnectButton = findViewById<View>(R.id.disconnect_button) as Button
        mDisconnectButton.setOnClickListener {
            if (mGatt != null) {
                mGatt!!.disconnect()
            }
        }

        mComValueTextView = findViewById<View>(R.id.value) as TextView

        // ListView and adapter used to display the list of detected devices
        listViewLE = findViewById<View>(R.id.discovery_list) as ListView
        listBluetoothDevice = ArrayList()
        listStringBluetoothDevice = ArrayList()
        adapterLeScanResult = ArrayAdapter(
                this, android.R.layout.simple_list_item_1, listStringBluetoothDevice!!)
        listViewLE!!.adapter = adapterLeScanResult
        listViewLE!!.onItemClickListener = scanResultOnItemClickListener

    }


    override fun onResume() {
        super.onResume()
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, RQS_ENABLE_BLUETOOTH)
        }

    }

    private fun getBTDeviceType(d: BluetoothDevice): String {
        var type = ""

        when (d.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> type = "DEVICE_TYPE_CLASSIC"
            BluetoothDevice.DEVICE_TYPE_DUAL -> type = "DEVICE_TYPE_DUAL"
            BluetoothDevice.DEVICE_TYPE_LE -> type = "DEVICE_TYPE_LE"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> type = "DEVICE_TYPE_UNKNOWN"
            else -> type = "unknown..."
        }

        return type
    }

    override fun onDestroy() {
        if (mGatt == null) {
            super.onDestroy()
        } else {
            //mGatt.close();
            try {
                mGatt!!.close()
            } catch (e: Exception) {
                Log.d(TAG, "close ignoring: " + e)
            }

            mGatt = null
            mHandler.removeCallbacks(mUIrunnable)
            super.onDestroy()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (requestCode == RQS_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }

        getBluetoothAdapterAndLeScanner()

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null)

            super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Method invoked when keyUp is clicked. Used to warn the user that he is about to close the application
     *
     * @param keyCode
     * @param event
     * @return
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to leave the application ? ")
        builder.setPositiveButton("Yes") { dialog, which -> finish() }
        builder.setNegativeButton("No") { dialog, which -> }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        return true
    }

    // Initialize the Bluetooth adapter and LEScanner used for basic LE functions like scan
    private fun getBluetoothAdapterAndLeScanner() {
        // Get BluetoothAdapter and BluetoothLeScanner.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner

        mScanning = false
    }

    //
    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Show the progress dialog while scan is in progress
            mProgressDialog!!.show()
            // Clear the list of devices if enable is true
            listBluetoothDevice!!.clear()
            listStringBluetoothDevice!!.clear()
            listViewLE!!.invalidateViews()

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed({
                mBluetoothLeScanner!!.stopScan(scanCallback)
                listViewLE!!.invalidateViews()

                Toast.makeText(this@bluetooth,
                        "Scan stopped",
                        Toast.LENGTH_LONG).show()

                mScanning = false
                mScanButton!!.isEnabled = true
                mProgressDialog!!.dismiss()
            }, SCAN_PERIOD)

            mBluetoothLeScanner!!.startScan(scanCallback)
            mScanning = true
            mScanButton!!.isEnabled = false
        } else {
            mBluetoothLeScanner!!.stopScan(scanCallback)
            mScanning = false
            mScanButton!!.isEnabled = true
        }
    }

    private fun addBluetoothDevice(device: BluetoothDevice) {
        //            Log.e("bluetooth", "name = " + device.getName() + "\naddress = " + device.getAddress());
        if (!listBluetoothDevice!!.contains(device)) {
            listBluetoothDevice!!.add(device)
            listStringBluetoothDevice!!.add(device.name + "\n"
                    + device.address)
            listViewLE!!.invalidateViews()
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallBack)
            mHandler2.post(mUIrunnable)

            //            startActivity(myIntent);

        }/* else {
            mGatt.disconnect();
        }*/
    }

    private fun setNotifications() {
        mGatt!!.setCharacteristicNotification(mComCharacteristic, true)

        val descriptor = mComCharacteristic!!.getDescriptor(UUID_COMMANDE)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        mGatt!!.writeDescriptor(descriptor)
    }

    private fun readCharacteristic() {
        if (mGatt != null && mComCharacteristic != null) {
            mGatt!!.readCharacteristic(mComCharacteristic)
        }
    }

    fun Go(v: View) {
        //on creer une nouvelle intent on definit la class de depart ici this et la class d'arrivé ici SecondActivite
        val intent = Intent(this, MainActivity::class.java)
        //on lance l'intent, cela a pour effet de stoper l'activité courante et lancer une autre activite ici SecondActivite
        startActivity(intent)


    }

    companion object {

        private val TAG = "ReadCharacteristic"

        val UUID_COMMANDE = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val RQS_ENABLE_BLUETOOTH = 1
        private val SCAN_PERIOD: Long = 5000
        var value = 0
            private set
        val SOME_KEY = "some_key"

        fun ceudruc(): Int {

            return 20
        }
    }

}
