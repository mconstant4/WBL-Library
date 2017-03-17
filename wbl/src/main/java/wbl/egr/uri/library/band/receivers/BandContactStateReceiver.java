package wbl.egr.uri.library.band.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by root on 3/17/17.
 */

public abstract class BandContactStateReceiver extends BroadcastReceiver {
    public static final String BAND_STATE = "uri.wbl.ear.band_state";
    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.wbl.ear.band_contact_state_receiver");
}