package wbl.egr.uri.wbl;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.band.receivers.BandUpdateStateReceiver;
import wbl.egr.uri.library.band.services.BandConnectionJobServiceBETA;
import wbl.egr.uri.library.band.tasks.RequestHeartRateTask;

/**
 * Created by root on 3/22/17.
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private JobScheduler mJobScheduler;

    private BandUpdateStateReceiver mBandUpdateStateReceiver = new BandUpdateStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, -1)) {
                case BandConnectionJobServiceBETA.STATE_CONNECTED:
                    log("Connected");
                    String[] sensors = {};
                    BandConnectionJobServiceBETA.startStream(mContext, mJobScheduler, true, sensors);
                    break;
                case BandConnectionJobServiceBETA.STATE_DISCONNECTED:
                    log("Disconnected");

                    break;
                case BandConnectionJobServiceBETA.STATE_BAND_NOT_WORN:
                    log("Band not Worn");
                    break;
                case BandConnectionJobServiceBETA.STATE_STREAMING:
                    log("Start Streaming");
                    break;
                case BandConnectionJobServiceBETA.STATE_BAND_OFF:
                    log("Band off");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = this;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        startService(new Intent(this, BandConnectionJobServiceBETA.class));
        registerReceiver(mBandUpdateStateReceiver, BandUpdateStateReceiver.INTENT_FILTER);
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    protected void onDestroy() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(mBandUpdateStateReceiver);
        mJobScheduler.cancelAll();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "pref_enable_band_collection":
                if (sharedPreferences.getBoolean(key, false)) {
                    new RequestHeartRateTask().execute(new WeakReference<Activity>(this));
                    BandConnectionJobServiceBETA.connect(this, mJobScheduler, true);
                } else {
                    BandConnectionJobServiceBETA.stopStream(this, mJobScheduler);
                    BandConnectionJobServiceBETA.disconnect(this, mJobScheduler);
                    //stopService(new Intent(this, BandConnectionJobServiceBETA.class));
                }
                break;
        }
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
