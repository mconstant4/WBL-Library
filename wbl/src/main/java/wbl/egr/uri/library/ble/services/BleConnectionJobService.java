package wbl.egr.uri.library.ble.services;

import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.UUID;

import wbl.egr.uri.library.ble.receivers.BleConnectionUpdateReceiver;
import wbl.egr.uri.library.ble.receivers.BleDeviceInfoReceiver;
import wbl.egr.uri.library.ble.receivers.BleValueUpdateReceiver;

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
    public static final String EXTRA_SERVICE = "uri.egr.wbl.library.ble_service";
    public static final String EXTRA_CHARACTERISTIC = "uri.egr.wbl.library.ble_characteristic";
    public static final String EXTRA_DATA = "uri.egr.wbl.library.ble_data";

    public static byte[] generateByteArray(int[] data) {
        //assumes 4 bytes per int
        byte[] result = new byte[data.length * 4];
        int index = 0;
        for (int i : data) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.putInt(i);
            byte[] array = byteBuffer.array();
            for (byte b : array) {
                result[index++] = b;
            }
        }
        return result;
    }

    public static int[] generateIntArray(byte[] data) {
        //Assumes each int is 4 bytes
        int[] result = new int[data.length / 4];
        int i = 0;
        for (int index = 0; index < data.length; index+=4) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.nativeOrder());
            byte[] b = {data[index], data[index+1], data[index+2], data[index+3]};
            byteBuffer.put(b);
            result[i++] = byteBuffer.getInt();
        }

        return result;
    }

    private static int mJobId = 0;

    public static void connect(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress) {
        if (context == null || jobScheduler == null || deviceAddress == null) {
            Log.d("BleConnectionJobService", "Connect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_CONNECT);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Connect Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BleConnectionJobService", "Connect Failed (Caller no longer Exists)");
        }
    }

    public static void discoverService(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress) {
        if (context == null || jobScheduler == null || deviceAddress == null) {
            Log.d("BleConnectionJobService", "Discover Services Failed (Discover Services called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_DISCOVER_SERVICES);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Discover Services Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BleConnectionJobService", "Discover Services Failed (Caller no longer Exists)");
        }
    }

    public static void enableNotifications(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress, String serviceUuid, String characteristicUuid) {
        if (context == null || jobScheduler == null || deviceAddress == null || serviceUuid == null || characteristicUuid == null) {
            Log.d("BleConnectionJobService", "Enable Notifications Failed (Called with Invalid Parameters");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_ENABLE_NOTIFICATIONS);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);
        bundle.putString(BleConnectionJobService.EXTRA_SERVICE, serviceUuid);
        bundle.putString(BleConnectionJobService.EXTRA_CHARACTERISTIC, characteristicUuid);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Error Scheduling Job");
            }
        } else {
            Log.d("BleConnectionJobService", "Enable Notifications Failed (Caller no longer Exists");
        }
    }

    public static void disableNotifications(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress, String serviceUuid, String characteristicUuid) {
        if (context == null || jobScheduler == null || deviceAddress == null || serviceUuid == null || characteristicUuid == null) {
            Log.d("BleConnectionJobService", "Disable Notifications Failed (Called with Invalid Parameters");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_DISABLE_NOTIFICATIONS);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);
        bundle.putString(BleConnectionJobService.EXTRA_SERVICE, serviceUuid);
        bundle.putString(BleConnectionJobService.EXTRA_CHARACTERISTIC, characteristicUuid);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Disable Notifications Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BleConnectionJobService", "Disable Notifications Failed (Caller no longer Exists)");
        }
    }

    public static void readCharacteristic(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress, String serviceUuid, String characteristicUuid) {
        if (context == null || jobScheduler == null || deviceAddress == null || serviceUuid == null || characteristicUuid == null) {
            Log.d("BleConnectionJobService", "Read Characteristic Failed (Called with Invalid Parameters");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_READ_CHARACTERISTIC);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);
        bundle.putString(BleConnectionJobService.EXTRA_SERVICE, serviceUuid);
        bundle.putString(BleConnectionJobService.EXTRA_CHARACTERISTIC, characteristicUuid);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Read Characteristic Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BleConnectionJobService", "Read Characteristic Failed (Caller no longer Exists)");
        }
    }

    public static void writeCharacteristic(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress, String serviceUuid, String characteristicUuid) {
        if (context == null || jobScheduler == null || deviceAddress == null || serviceUuid == null || characteristicUuid == null) {
            Log.d("BleConnectionJobService", "Write Characteristic Failed (Called with Invalid Parameters");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_WRITE_CHARACTERISTIC);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);
        bundle.putString(BleConnectionJobService.EXTRA_SERVICE, serviceUuid);
        bundle.putString(BleConnectionJobService.EXTRA_CHARACTERISTIC, characteristicUuid);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Write Characteristic Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BleConnectionJobService", "Write Characteristic Failed (Caller no longer Exists)");
        }
    }

    public static void disconnect(WeakReference<Context> context, JobScheduler jobScheduler, String deviceAddress) {
        if (context == null || jobScheduler == null || deviceAddress == null) {
            Log.d("BleConnectionJobService", "Disconnect Failed (Called with Invalid Parameters");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_DISCONNECT);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, deviceAddress);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BleConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            //Keep extra delay for disconnect to ensure other actions are performed before this is executed
            builder.setOverrideDeadline(100);
            builder.setPersisted(false);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BleConnectionJobService", "Disconnect Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BleConnectionJobService", "Disconnect Failed (Caller no longer Exists)");
        }
    }

    private final int NOTIFICATION_ID = 347;

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, BluetoothGatt> mConnectedDevices;

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTING:
                        log("Establishing connection to " + gatt.getDevice() + "...");
                        sendBroadcast(gatt.getDevice().getAddress(), BleConnectionUpdateReceiver.UPDATE_CONNECTING);
                        break;
                    case BluetoothGatt.STATE_CONNECTED:
                        log("Connected to " + gatt.getDevice());
                        mConnectedDevices.put(gatt.getDevice().getAddress(), gatt);

                        //Update Connection State
                        sendBroadcast(gatt.getDevice().getAddress(), BleConnectionUpdateReceiver.UPDATE_CONNECTED);

                        //Update Device Info
                        sendBroadcast(gatt.getDevice().getAddress(), gatt.getDevice().getName(), gatt.getDevice().getBluetoothClass().getMajorDeviceClass());
                        break;
                    case BluetoothGatt.STATE_DISCONNECTING:
                        log("Disconnecting from " + gatt.getDevice() + "...");
                        sendBroadcast(gatt.getDevice().getAddress(), BleConnectionUpdateReceiver.UPDATE_DISCONNECTING);
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        log("Disconnected from " + gatt.getDevice());
                        mConnectedDevices.remove(gatt.getDevice().getAddress());
                        if (mConnectedDevices.size() == 0) {
                            stopSelf();
                        } else {
                            log(mConnectedDevices.size() + " Devices still connected.");
                        }

                        sendBroadcast(gatt.getDevice().getAddress(), BleConnectionUpdateReceiver.UPDATE_DISCONNECTED);
                        break;
                }
            } else {
                log("Gatt Connection Error (" + status +  ")");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Services Discovered from " + gatt.getDevice() + "\n");
                for (BluetoothGattService service : gatt.getServices()) {
                    log("\tService: " + service.getUuid());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        log("\t\tCharacteristic: " + characteristic.getUuid());
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                            log("\t\t\tDescriptor: " + descriptor.getUuid());
                        }
                    }
                }

                //Send Broadcast
                sendBroadcast(gatt.getDevice().getAddress(), BleConnectionUpdateReceiver.UPDATE_SERVICES_DISCOVERED);
            } else {
                log("Bluetooth Gatt Error (" + status + ")");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Characteristic Read from " + gatt.getDevice());
                sendBroadcast(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), characteristic.getValue());
            } else {
                log("Bluetooth Gatt Error (" + status + ")");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Characteristic Wrote to " + gatt.getDevice());
            } else {
                log("Bluetooth Gatt Error (" + status + ")");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            log("Characteristic Changed");
            sendBroadcast(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), characteristic.getValue());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Descriptor Read from " + gatt.getDevice());
            } else {
                log("Bluetooth Gatt Error (" + status + ")");
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Descriptor Wrote to " + gatt.getDevice());
            } else {
                log("Bluetooth Gatt Error (" + status + ")");
            }
        }
    };

    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg ) {
            log("Handling Message");
            JobParameters parameters = (JobParameters) msg.obj;
            BluetoothGatt gatt;
            String deviceAddress;
            String characteristicUuid;
            String serviceUuid;
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
                    gatt = mConnectedDevices.get(deviceAddress);
                    gatt.disconnect();
                    gatt.close();
                    jobFinished(parameters, false);
                    break;
                case ACTION_DISCOVER_SERVICES:
                    log("action discover services");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    gatt = mConnectedDevices.get(deviceAddress);
                    gatt.discoverServices();
                    jobFinished(parameters, false);
                    break;
                case ACTION_ENABLE_NOTIFICATIONS:
                    log("action enable notifications");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    serviceUuid = parameters.getExtras().getString(EXTRA_SERVICE);
                    characteristicUuid = parameters.getExtras().getString(EXTRA_CHARACTERISTIC);
                    gatt = mConnectedDevices.get(deviceAddress);
                    BluetoothGattService service = gatt.getService(UUID.fromString(serviceUuid));
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUuid));
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    gatt.setCharacteristicNotification(characteristic, true);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    jobFinished(parameters, false);
                    break;
                case ACTION_DISABLE_NOTIFICATIONS:
                    log("action disable notifications");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    serviceUuid = parameters.getExtras().getString(EXTRA_SERVICE);
                    characteristicUuid = parameters.getExtras().getString(EXTRA_CHARACTERISTIC);
                    gatt = mConnectedDevices.get(deviceAddress);
                    BluetoothGattService disableService = gatt.getService(UUID.fromString(serviceUuid));
                    BluetoothGattCharacteristic disableCharacteristic = disableService.getCharacteristic(UUID.fromString(characteristicUuid));
                    BluetoothGattDescriptor disableDescriptor = disableCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    gatt.setCharacteristicNotification(disableCharacteristic, false);
                    disableCharacteristic.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(disableDescriptor);
                    jobFinished(parameters, false);
                    break;
                case ACTION_READ_CHARACTERISTIC:
                    log("action read characteristic");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    serviceUuid = parameters.getExtras().getString(EXTRA_SERVICE);
                    characteristicUuid = parameters.getExtras().getString(EXTRA_CHARACTERISTIC);
                    gatt = mConnectedDevices.get(deviceAddress);
                    if (gatt != null) {
                        BluetoothGattService readService = gatt.getService(UUID.fromString(serviceUuid));
                        BluetoothGattCharacteristic readCharacteristic = readService.getCharacteristic(UUID.fromString(characteristicUuid));
                        gatt.readCharacteristic(readCharacteristic);
                    }
                    jobFinished(parameters, false);
                    break;
                case ACTION_WRITE_CHARACTERISTIC:
                    log("action write characteristic");
                    deviceAddress = parameters.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    serviceUuid = parameters.getExtras().getString(EXTRA_SERVICE);
                    characteristicUuid = parameters.getExtras().getString(EXTRA_CHARACTERISTIC);
                    int[] data = parameters.getExtras().getIntArray(EXTRA_DATA);
                    gatt = mConnectedDevices.get(deviceAddress);
                    BluetoothGattService writeService = gatt.getService(UUID.fromString(serviceUuid));
                    BluetoothGattCharacteristic writeCharacteristic = writeService.getCharacteristic(UUID.fromString(characteristicUuid));
                    writeCharacteristic.setValue(generateByteArray(data));
                    gatt.readCharacteristic(writeCharacteristic);
                    jobFinished(parameters, false);
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

    private void sendBroadcast(String deviceAddress, int connectionState) {
        Intent intent = new Intent(BleConnectionUpdateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BleConnectionUpdateReceiver.EXTRA_DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(BleConnectionUpdateReceiver.EXTRA_STATE, connectionState);
        sendBroadcast(intent);
    }

    private void sendBroadcast(String deviceAddress, String characteristicUuid, byte[] data) {
        Intent intent = new Intent(BleValueUpdateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BleValueUpdateReceiver.EXTRA_DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(BleValueUpdateReceiver.EXTRA_CHARACTERISTIC, characteristicUuid);
        intent.putExtra(BleValueUpdateReceiver.EXTRA_DATA, data);
        sendBroadcast(intent);
    }

    private void sendBroadcast(String deviceAddress, String deviceName, int deviceType) {
        Intent intent = new Intent(BleDeviceInfoReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BleDeviceInfoReceiver.EXTRA_DEVICE_NAME, deviceName);
        intent.putExtra(BleDeviceInfoReceiver.EXTRA_DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(BleDeviceInfoReceiver.EXTRA_DEVICE_TYPE, deviceType);
        sendBroadcast(intent);
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
