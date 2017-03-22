package wbl.egr.uri.library.band.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by root on 3/17/17.
 */

public abstract class BandUpdateStateReceiver extends BroadcastReceiver {
    public static final String EXTRA_NEW_STATE = "uri.egr.wbl.library.band_update_state";

    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.wbl.ear.band_update_state_receiver");
}