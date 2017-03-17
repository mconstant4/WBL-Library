package wbl.egr.uri.wbl;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.ble.receivers.BleConnectionUpdateReceiver;
import wbl.egr.uri.library.ble.receivers.BleDeviceInfoReceiver;
import wbl.egr.uri.library.ble.receivers.BleValueUpdateReceiver;
import wbl.egr.uri.library.ble.services.BleConnectionJobService;

public class BleDemoActivity extends AppCompatActivity {
    private JobScheduler mJobScheduler;
    private Context mContext;
    private BleConnectionUpdateReceiver mBleConnectionUpdateReceiver = new BleConnectionUpdateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deviceAddress = intent.getStringExtra(BleConnectionUpdateReceiver.EXTRA_DEVICE_ADDRESS);
            int state = intent.getIntExtra(BleConnectionUpdateReceiver.EXTRA_STATE, -1);

            switch (state) {
                case BleConnectionUpdateReceiver.UPDATE_CONNECTING:
                    log("Connecting to " + deviceAddress);
                    break;
                case BleConnectionUpdateReceiver.UPDATE_CONNECTED:
                    log("Connected to " + deviceAddress);
                    BleConnectionJobService.discoverService(new WeakReference<Context>(mContext), mJobScheduler, deviceAddress);
                    break;
                case BleConnectionUpdateReceiver.UPDATE_SERVICES_DISCOVERED:
                    log("Discovered Services from " + deviceAddress);
                    BleConnectionJobService.enableNotifications(new WeakReference<Context>(mContext), mJobScheduler, deviceAddress, "0000180f-0000-1000-8000-00805f9b34fb", "00002a19-0000-1000-8000-00805f9b34fb");
                    break;
                case BleConnectionUpdateReceiver.UPDATE_DISCONNECTING:
                    log("Disconnecting from " + deviceAddress);
                    break;
                case BleConnectionUpdateReceiver.UPDATE_DISCONNECTED:
                    log("Disconnected from " + deviceAddress);
                    break;
            }
        }
    };

    private BleValueUpdateReceiver mBleValueUpdateReceiver = new BleValueUpdateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deviceAddress = intent.getStringExtra(BleValueUpdateReceiver.EXTRA_DEVICE_ADDRESS);
            String characteristicUuid = intent.getStringExtra(BleValueUpdateReceiver.EXTRA_CHARACTERISTIC);
            byte[] data = intent.getByteArrayExtra(BleValueUpdateReceiver.EXTRA_DATA);
            log("Battery Level: " + data[0]);
        }
    };

    private BleDeviceInfoReceiver mBleDeviceInfoReceiver = new BleDeviceInfoReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deviceName = intent.getStringExtra(BleDeviceInfoReceiver.EXTRA_DEVICE_NAME);
            String deviceAddress = intent.getStringExtra(BleDeviceInfoReceiver.EXTRA_DEVICE_ADDRESS);
            int deviceType = intent.getIntExtra(BleDeviceInfoReceiver.EXTRA_DEVICE_TYPE, -1);
            log("Connected Device Info:\n" +
                    "\tDevice Name: " + deviceName + "\n" +
                    "\tDevice Address: " + deviceAddress + "\n" +
                    "\tDevice Type: " + deviceType);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        startService(new Intent(this, BleConnectionJobService.class));

        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mBleDeviceInfoReceiver, BleDeviceInfoReceiver.INTENT_FILTER);
        registerReceiver(mBleValueUpdateReceiver, BleValueUpdateReceiver.INTENT_FILTER);
        registerReceiver(mBleConnectionUpdateReceiver, BleConnectionUpdateReceiver.INTENT_FILTER);
        BleConnectionJobService.connect(new WeakReference<Context>(mContext), mJobScheduler, "98:4F:EE:0F:A0:DE");
    }

    @Override
    protected void onStop() {
        super.onStop();

        BleConnectionJobService.disableNotifications(new WeakReference<Context>(mContext), mJobScheduler, "98:4F:EE:0F:A0:DE", "0000180f-0000-1000-8000-00805f9b34fb", "00002a19-0000-1000-8000-00805f9b34fb");
        BleConnectionJobService.disconnect(new WeakReference<Context>(mContext), mJobScheduler, "98:4F:EE:0F:A0:DE");
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBleDeviceInfoReceiver);
        unregisterReceiver(mBleConnectionUpdateReceiver);
        unregisterReceiver(mBleValueUpdateReceiver);
        super.onDestroy();
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}