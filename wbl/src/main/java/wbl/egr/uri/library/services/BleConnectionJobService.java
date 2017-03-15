package wbl.egr.uri.library.services;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by mconstant on 3/15/17.
 */

public class BleConnectionJobService extends JobService {
    public static final String ACTION_TYPE = "uri.egr.wbl.library.ble_action_type";
    public static final int ACTION_CONNECT = 0;
    public static final int ACTION_DISCONNECT = 1;
    public static final int ACTION_DISCOVER_SERVICES = 2;
    public static final int ACTION_ENABLE_NOTIFICATIONS = 3;
    public static final int ACTION_DISABLE_NOTIFICATIONS = 4;
    public static final int ACTION_READ_CHARACTERISTIC = 5;
    public static final int ACTION_WRITE_CHARACTERISTIC = 6;
    public static final String EXTRA_DEVICE_ADDRESS = "uri.egr.wbl.library.ble_device_address";
    public static final String EXTRA_CHARACTERISTIC = "uri.egr.wbl.library.ble_characteristic";

    private final int NOTIFICATION_ID = 347;

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, BluetoothGatt> mConnectedDevices;

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        log("Connected to " + gatt.getDevice().getName());
                        mConnectedDevices.put(gatt.getDevice().getAddress(), gatt);
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        log("Disconnected from " + gatt.getDevice().getName());
                        mConnectedDevices.remove(gatt);
                        if (mConnectedDevices.size() == 0) {
                            stopSelf();
                        }
                        break;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg ) {
            log("Handling Message");
            JobParameters parameters = (JobParameters) msg.obj;
            String deviceAddress;
            String characteristicUuid;
            switch (parameters.getExtras().getInt(ACTION_TYPE, -1)) {
                case ACTION_CONNECT:
                    log("action connect");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                    device.connectGatt(mContext, true, mBluetoothGattCallback);
                    jobFinished(parameters, false);
                    break;
                case ACTION_DISCONNECT:
                    log("action disconnect");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    BluetoothGatt gatt = mConnectedDevices.get(deviceAddress);
                    gatt.disconnect();
                    jobFinished(parameters, false);
                    break;
                case ACTION_DISCOVER_SERVICES:

                    break;
                case ACTION_ENABLE_NOTIFICATIONS:

                    break;
                case ACTION_DISABLE_NOTIFICATIONS:

                    break;
                case ACTION_READ_CHARACTERISTIC:

                    break;
                case ACTION_WRITE_CHARACTERISTIC:

                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        log("Service Created");

        mContext = this;
        mConnectedDevices = new HashMap<>();

        //Initialize Bluetooth Adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //Declare as Foreground Service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("WBL BLE Service is Running")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentText("Touch to Disconnect")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }



    @Override
    public boolean onStartJob(JobParameters params) {
        log("Job Started");
        mJobHandler.sendMessage(Message.obtain(mJobHandler, (int) Math.round(Math.random()), params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        log("Job Stopped");
        mJobHandler.removeMessages(params.getJobId());
        return false;
    }

    @Override
    public void onDestroy() {
        log("Service Destroyed");

        super.onDestroy();
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
