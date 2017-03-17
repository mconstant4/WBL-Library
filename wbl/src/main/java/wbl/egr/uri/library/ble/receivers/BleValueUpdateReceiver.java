package wbl.egr.uri.library.ble.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by root on 3/16/17.
 */

public abstract class BleValueUpdateReceiver extends BroadcastReceiver {
    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.egr.wbl.library.ble_value_update_receiver");
    public static final String EXTRA_DEVICE_ADDRESS = "uri.egr.wbl.library.ble_device_address";
    public static final String EXTRA_CHARACTERISTIC = "uri.egr.wbl.library.ble_characteristic";
    public static final String EXTRA_DATA = "uri.egr.wbl.library.ble_value";
}
