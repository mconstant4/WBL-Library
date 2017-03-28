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

import wbl.egr.uri.library.band.models.SensorModel;
import wbl.egr.uri.library.band.receivers.BandInfoReceiver;
import wbl.egr.uri.library.band.receivers.BandUpdateStateReceiver;
import wbl.egr.uri.library.band.services.BandConnectionJobService;
import wbl.egr.uri.library.band.tasks.RequestHeartRateTask;

/**
 * Created by root on 3/22/17.
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences mSharedPreferences;
    private WeakReference<Context> mContext;
    private JobScheduler mJobScheduler;

    private BandUpdateStateReceiver mBandUpdateStateReceiver = new BandUpdateStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, -1)) {
                case BandConnectionJobService.STATE_CONNECTED:
                    log("Connected");
                    BandConnectionJobService.requestBandInfo(mContext);
                    String[] sensors = {SensorModel.SENSOR_ACCELEROMETER, SensorModel.SENSOR_HEART_RATE, SensorModel.SENSOR_DISTANCE};
                    BandConnectionJobService.startStream(mContext, false, sensors);
                    break;
                case BandConnectionJobService.STATE_DISCONNECTED:
                    log("Disconnected");

                    break;
                case BandConnectionJobService.STATE_BAND_NOT_WORN:
                    log("Band not Worn");
                    break;
                case BandConnectionJobService.STATE_STREAMING:
                    log("Start Streaming");
                    break;
                case BandConnectionJobService.STATE_BAND_OFF:
                    log("Band off");
                    break;
            }
        }
    };

    private BandInfoReceiver mBandInfoReceiver = new BandInfoReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] bandInfo = intent.getStringArrayExtra(BandInfoReceiver.EXTRA_INFO);
            log("FW: " + bandInfo[0] + "\n" +
                "HW: " + bandInfo[1] + "\n" +
                "Name: " + bandInfo[2] + "\n" +
                "Address: " + bandInfo[3]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = new WeakReference<Context>(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        registerReceiver(mBandUpdateStateReceiver, BandUpdateStateReceiver.INTENT_FILTER);
        registerReceiver(mBandInfoReceiver, BandInfoReceiver.INTENT_FILTER);
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    protected void onDestroy() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(mBandInfoReceiver);
        unregisterReceiver(mBandUpdateStateReceiver);
        mJobScheduler.cancelAll();
        mContext.clear();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "pref_enable_band_collection":
                if (sharedPreferences.getBoolean(key, false)) {
                    new RequestHeartRateTask().execute(new WeakReference<Activity>(this));
                    BandConnectionJobService.startService(this);
                    BandConnectionJobService.connect(mContext, true);
                } else {
                    BandConnectionJobService.stopStream(mContext);
                    BandConnectionJobService.disconnect(mContext);
                }
                break;
        }
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
