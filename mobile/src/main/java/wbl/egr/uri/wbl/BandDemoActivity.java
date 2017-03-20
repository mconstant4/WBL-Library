package wbl.egr.uri.wbl;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.band.receivers.BandUpdateReceiver;
import wbl.egr.uri.library.band.services.BandConnectionJobService;
import wbl.egr.uri.library.band.tasks.RequestHeartRateTask;

/**
 * Created by root on 3/17/17.
 */

public class BandDemoActivity extends AppCompatActivity {
    private WeakReference<Context> mContext;
    private JobScheduler mJobScheduler;

    private BandUpdateReceiver mBandUpdateReceiver = new BandUpdateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DEMO", "Update Received");
            if (intent.hasExtra(BandUpdateReceiver.UPDATE_BAND_CONNECTED)) {
                BandConnectionJobService.startStreaming(mContext, mJobScheduler);
            } else if (intent.hasExtra(BandUpdateReceiver.UPDATE_BAND_DISCONNECTED)) {
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
        registerReceiver(mBandUpdateReceiver, BandUpdateReceiver.INTENT_FILTER);

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
    }
}
