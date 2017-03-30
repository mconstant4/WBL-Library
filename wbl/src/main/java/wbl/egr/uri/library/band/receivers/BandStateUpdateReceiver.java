package wbl.egr.uri.library.band.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by root on 3/29/17.
 */

public abstract class BandStateUpdateReceiver extends BroadcastReceiver {
    public static final IntentFilter INTENT_FILTER = new IntentFilter("wbl.band.state_update_receiver");

    public static final String EXTRA_STATE = "wbl.band.state_update";
}
