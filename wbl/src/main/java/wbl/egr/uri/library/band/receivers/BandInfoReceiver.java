package wbl.egr.uri.library.band.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by mconstant on 3/22/17.
 */

public abstract class BandInfoReceiver extends BroadcastReceiver {
    public static final String EXTRA_INFO = "uri.egr.wbl.library.band_info";

    public static final IntentFilter INTENT_FILTER = new IntentFilter("uri.wbl.ear.band_info_receiver");
}
