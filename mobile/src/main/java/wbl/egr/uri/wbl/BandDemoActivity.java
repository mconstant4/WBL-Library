package wbl.egr.uri.wbl;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.band.receivers.BandUpdateStateReceiver;
import wbl.egr.uri.library.band.services.BandConnectionJobServiceBETA;
import wbl.egr.uri.library.band.tasks.RequestHeartRateTask;

/**
 * Created by root on 3/17/17.
 *
 */

public class BandDemoActivity extends AppCompatActivity {
    /*private WeakReference<Context> mContext;
    private JobScheduler mJobScheduler;

    private BandUpdateStateReceiver mBandUpdateReceiver = new BandUpdateStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DEMO", "Update Received");
            if (intent.hasExtra(BandUpdateStateReceiver.UPDATE_BAND_CONNECTED)) {
                BandConnectionJobService.startStreaming(mContext, mJobScheduler);
            } else if (intent.hasExtra(BandUpdateStateReceiver.UPDATE_BAND_DISCONNECTED)) {
                Log.d("DEMO", "BAND DISCONNECTED");
                mJobScheduler.cancelAll();
                stopService(new Intent(mContext.get(), BandConnectionJobService.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = new WeakReference<Context>(this);

        startService(new Intent(this, BandConnectionJobService.class));

        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        registerReceiver(mBandUpdateReceiver, BandUpdateStateReceiver.INTENT_FILTER);

        new RequestHeartRateTask().execute(new WeakReference<Activity>(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        BandConnectionJobService.connect(mContext, mJobScheduler);
    }

    @Override
    protected void onStop() {
        super.onStop();

        BandConnectionJobService.disconnect(mContext, mJobScheduler);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBandUpdateReceiver);

        super.onDestroy();
    }*/

    private Context mContext;
    private JobScheduler mJobScheduler;

    private BandUpdateStateReceiver mBandUpdateStateReceiver = new BandUpdateStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, -1)) {
                case BandConnectionJobServiceBETA.STATE_CONNECTED:
                    log("Connected");
                    String[] sensors = {};
                    BandConnectionJobServiceBETA.startStream(new WeakReference<Context>(mContext), true, sensors);
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
        setContentView(R.layout.activity_main);

        new RequestHeartRateTask().execute(new WeakReference<Activity>(this));

        startService(new Intent(this, BandConnectionJobServiceBETA.class));
        registerReceiver(mBandUpdateStateReceiver, BandUpdateStateReceiver.INTENT_FILTER);
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        mContext = this;

        BandConnectionJobServiceBETA.connect(new WeakReference<Context>(mContext), true);
    }

    @Override
    protected void onDestroy() {
        BandConnectionJobServiceBETA.stopStream(new WeakReference<Context>(mContext));
        BandConnectionJobServiceBETA.disconnect(new WeakReference<Context>(mContext));
        unregisterReceiver(mBandUpdateStateReceiver);
        mJobScheduler.cancelAll();
        stopService(new Intent(this, BandConnectionJobServiceBETA.class));

        super.onDestroy();
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
