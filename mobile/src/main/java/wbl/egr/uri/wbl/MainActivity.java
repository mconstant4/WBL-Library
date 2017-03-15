package wbl.egr.uri.wbl;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import wbl.egr.uri.library.services.BleConnectionJobService;

public class MainActivity extends AppCompatActivity {
    private JobScheduler mJobScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BleConnectionJobService.class));

        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_CONNECT);
        bundle.putString(BleConnectionJobService.EXTRA_DEVICE_ADDRESS, "98:4F:EE:0F:A0:DE");

        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), BleConnectionJobService.class.getName()));
        builder.setExtras(bundle);
        builder.setOverrideDeadline(100);
        if(mJobScheduler.schedule(builder.build()) <= 0) {
            Log.d(this.getClass().getSimpleName(), "Something Went Wrong");
        }
    }

    @Override
    protected void onDestroy() {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BleConnectionJobService.ACTION_TYPE, BleConnectionJobService.ACTION_DISCONNECT);

        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), BleConnectionJobService.class.getName()));
        builder.setExtras(bundle);
        builder.setOverrideDeadline(100);
        if(mJobScheduler.schedule(builder.build()) <= 0) {
            Log.d(this.getClass().getSimpleName(), "Something Went Wrong");
        }

        super.onDestroy();
    }
}