package wbl.egr.uri.library.ble.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by root on 3/16/17.
 */

public abstract class BleConnectionUpdateReceiver extends BroadcastReceiver {
    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.egr.wbl.library.ble_connection_update_filter");
    public static final String EXTRA_STATE = "uri.egr.wbl.library.ble_connection_update";
    public static final String EXTRA_DEVICE_ADDRESS = "uri.egr.wbl.library.ble_device_address";

    public static final int UPDATE_CONNECTING = 0;
    public static final int UPDATE_CONNECTED = 1;
    public static final int UPDATE_DISCONNECTING = 2;
    public static final int UPDATE_DISCONNECTED = 3;
    public static final int UPDATE_SERVICES_DISCOVERED = 4;
}
