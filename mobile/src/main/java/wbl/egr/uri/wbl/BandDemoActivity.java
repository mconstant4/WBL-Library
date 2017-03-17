package wbl.egr.uri.wbl;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.band.services.BandConnectionJobService;

/**
 * Created by root on 3/17/17.
 */

public class BandDemoActivity extends AppCompatActivity {
    private JobScheduler mJobScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BandConnectionJobService.class));

        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        BandConnectionJobService.connect(new WeakReference<Context>(this), mJobScheduler, BandConnectionJobService.ACTION_CONNECT);
    }

    @Override
    protected void onStop() {
        super.onStop();

        BandConnectionJobService.disconnect(new WeakReference<Context>(this), mJobScheduler, BandConnectionJobService.ACTION_DISCONNECT);
    }
}
