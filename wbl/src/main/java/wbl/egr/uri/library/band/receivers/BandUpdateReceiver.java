package wbl.egr.uri.library.band.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by root on 3/17/17.
 */

public abstract class BandUpdateReceiver extends BroadcastReceiver {
    public static final String UPDATE_BAND_CONNECTED = "uri.wbl.ear.action_band_connected";
    public static final String UPDATE_BAND_DISCONNECTED = "uri.wbl.ear.action_band_disconnected";
    public static final String UPDATE_BAND_STREAMING = "uri.wbl.ear.action_band_streaming";
    public static final String UPDATE_BAND_INFO = "uri.wbl.ear.action_band_info";

    public static final String EXTRA_BAND_INFO = "uri.wbl.ear.extra_band_info";

    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.wbl.ear.band_update_receiver");
}