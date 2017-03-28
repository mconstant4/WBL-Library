package wbl.egr.uri.library.band.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.band.services.BandConnectionJobService;

/**
 * Created by mconstant on 3/25/17.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NOTIFICATION", "STOP");
        BandConnectionJobService.stopService(new WeakReference<Context>(context));
    }
}