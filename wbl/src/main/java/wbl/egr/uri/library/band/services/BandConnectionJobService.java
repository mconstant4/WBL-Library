package wbl.egr.uri.library.band.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandResultCallback;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.notifications.VibrationType;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.GsrSampleRate;
import com.microsoft.band.sensors.SampleRate;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import wbl.egr.uri.library.band.band_listeners.BandAccelerometerListener;
import wbl.egr.uri.library.band.band_listeners.BandAmbientLightListener;
import wbl.egr.uri.library.band.band_listeners.BandContactListener;
import wbl.egr.uri.library.band.band_listeners.BandGsrListener;
import wbl.egr.uri.library.band.band_listeners.BandHeartRateListener;
import wbl.egr.uri.library.band.band_listeners.BandRRIntervalListener;
import wbl.egr.uri.library.band.band_listeners.BandSkinTemperatureListener;
import wbl.egr.uri.library.band.receivers.BandContactStateReceiver;
import wbl.egr.uri.library.band.receivers.BandUpdateStateReceiver;

/**
 * Created by root on 3/17/17.
 */

public class BandConnectionJobService extends JobService {
    /*public static final String ACTION_TYPE = "uri.egr.wbl.library.band_action_type";
    public static final int ACTION_CONNECT = 0;
    public static final int ACTION_DISCONNECT = 1;
    public static final int ACTION_START_STREAM = 2;
    public static final int ACTION_STOP_STREAM = 3;
    public static final int ACTION_REQUEST_INFO = 4;

    private static int mJobId = 0;

    public static void connect(WeakReference<Context> context, JobScheduler jobScheduler) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Connect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobService.ACTION_TYPE, BandConnectionJobService.ACTION_CONNECT);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Connect Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BandConnectionService", "Connect Failed (Caller no longer Exists)");
        }
    }

    public static void disconnect(WeakReference<Context> context, JobScheduler jobScheduler) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Connect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobService.ACTION_TYPE, BandConnectionJobService.ACTION_DISCONNECT);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Connect Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BandConnectionService", "Connect Failed (Caller no longer Exists)");
        }
    }

    public static void startStreaming(WeakReference<Context> context, JobScheduler jobScheduler) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Connect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobService.ACTION_TYPE, BandConnectionJobService.ACTION_START_STREAM);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Connect Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BandConnectionService", "Connect Failed (Caller no longer Exists)");
        }
    }

    public static void stopStreaming(WeakReference<Context> context, JobScheduler jobScheduler) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Connect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobService.ACTION_TYPE, BandConnectionJobService.ACTION_STOP_STREAM);

        if (context != null && context.get() != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mJobId++, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Connect Failed (Error Scheduling Job)");
            }
        } else {
            Log.d("BandConnectionService", "Connect Failed (Caller no longer Exists)");
        }
    }

    //States
    private static final int STATE_CONNECTED = 0;
    private static final int STATE_STREAMING = 1;
    private static final int STATE_DISCONNECTED = 2;
    private static final int STATE_OTHER = 3;

    private final int NOTIFICATION_ID = 437;

    private Context mContext;
    private int mState;
    private BandClientManager mBandClientManager;
    private BandClient mBandClient;
    private String mBandName;
    private String mBandAddress;

    private BandAccelerometerListener mBandAccelerometerListener;
    private BandAmbientLightListener mBandAmbientLightListener;
    private BandContactListener mBandContactListener;
    private BandGsrListener mBandGsrListener;
    private BandHeartRateListener mBandHeartRateListener;
    private BandRRIntervalListener mBandRRIntervalListener;
    private BandSkinTemperatureListener mBandSkinTemperatureListener;

    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            log("Handling Message");
            JobParameters parameters = (JobParameters) msg.obj;

            switch (parameters.getExtras().getInt(ACTION_TYPE, -1)) {
                case ACTION_CONNECT:
                    connect();
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
                case ACTION_START_STREAM:
                    startStreaming();
                    break;
                case ACTION_STOP_STREAM:
                    stopStreaming();
                    break;
                case ACTION_REQUEST_INFO:
                    getInfo();
                    break;
            }

            return false;
        }
    });

    BandContactStateReceiver mBandContactStateReceiver = new BandContactStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(BAND_STATE, false)) {
                resumeFromDynamicBlackout();
            } else {
                enterDynamicBlackout();
            }
        }
    };

    private BandResultCallback<ConnectionState> mBandConnectResultCallback = new BandResultCallback<ConnectionState>() {
        @Override
        public void onResult(ConnectionState connectionState, Throwable throwable) {
            Intent intent;
            switch (connectionState) {
                case CONNECTED:
                    log("Connected");
                    mState = STATE_CONNECTED;
                    updateNotification("CONNECTED");
                    try {
                        mBandClient.getNotificationManager().vibrate(VibrationType.RAMP_UP);
                    } catch (BandException e) {
                        e.printStackTrace();
                    }

                    //Broadcast Update
                    log("Broadcasting Update");
                    intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateStateReceiver.UPDATE_BAND_CONNECTED, true);
                    sendBroadcast(intent);
                    break;
                case BOUND:
                    log("Bound");
                    mState = STATE_DISCONNECTED;
                    updateNotification("DISCONNECTED");
                    Toast.makeText(mContext, "Could not connect to Band", Toast.LENGTH_LONG).show();
                    disconnect();
                    //Broadcast Update
                    log("Broadcasting Update");
                    intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateStateReceiver.UPDATE_BAND_DISCONNECTED, true);
                    sendBroadcast(intent);
                    updateNotification("Band Disconnected");
                    break;
                case BINDING:
                    log("Binding");
                    break;
                case UNBOUND:
                    log("Unbound");
                    mState = STATE_OTHER;
                    updateNotification("UNBOUND");
                    Toast.makeText(mContext, "Could not connect to Band", Toast.LENGTH_LONG).show();
                    //Send Broadcast
                    intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateStateReceiver.UPDATE_BAND_DISCONNECTED, true);
                    sendBroadcast(intent);
                    updateNotification("Band Disconnected");

                    stopSelf();
                    break;
                case UNBINDING:
                    log("Unbinding");
                    break;
                default:
                    log("Unknown State");
                    updateNotification("ERROR");
                    break;
            }
        }
    };

    private BandResultCallback<Void> mBandDisconnectResultCallback = new BandResultCallback<Void>() {
        @Override
        public void onResult(Void aVoid, Throwable throwable) {
            log("Disconnected");
            updateNotification("Band Disconnected");
            //Send Broadcast
            Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
            intent.putExtra(BandUpdateStateReceiver.UPDATE_BAND_DISCONNECTED, true);
            sendBroadcast(intent);
        }
    };
*/
    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return true;
        }
    });
    @Override
    public void onCreate() {
        super.onCreate();
        log("Service Created");
/*
        mContext = this;
        mState = STATE_OTHER;

        mBandClientManager = BandClientManager.getInstance();

        //Declare as Foreground Service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("WBL Band Service is Running")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentText("Touch to Disconnect")
                .setSubText("Band Status: Initializing")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        mBandAccelerometerListener = new BandAccelerometerListener(this);
        mBandAmbientLightListener = new BandAmbientLightListener(this);
        mBandContactListener = new BandContactListener(this);
        mBandGsrListener = new BandGsrListener(this);
        mBandHeartRateListener = new BandHeartRateListener(this);
        mBandRRIntervalListener = new BandRRIntervalListener(this);
        mBandSkinTemperatureListener = new BandSkinTemperatureListener(this);

        registerReceiver(mBandContactStateReceiver, BandContactStateReceiver.INTENT_FILTER);*/
    }



    @Override
    public boolean onStartJob(JobParameters params) {
        log("Job Started");
        mJobHandler.sendMessage(Message.obtain(mJobHandler, (int) Math.round(Math.random()), params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        log("Job Stopped");
        mJobHandler.removeMessages(params.getJobId());
        return false;
    }

    @Override
    public void onDestroy() {
        log("Service Destroyed");

        super.onDestroy();
    }
/*
    private void connect() {
        if (mBandClientManager == null) {
            log("Connect Failed (Band Client Manager not Initialized)");
            return;
        }

        BandInfo[] pairedBands = mBandClientManager.getPairedBands();
        if (pairedBands == null || pairedBands.length == 0) {
            log("Connect Failed (No Bands are Paired with this Device)");
        } else if (pairedBands.length > 1) {
            /**
             * TODO
             * Implement UI to allow User to choose Band to pair to.
             * For now, always choose pairedBands[0]
             */
           /* connect(pairedBands[0]);
        } else {
            connect(pairedBands[0]);
        }
    }

    private void connect(BandInfo bandInfo) {
        log("Attempting to Connect to " + bandInfo.getMacAddress() + "...");
        mBandName = bandInfo.getName();
        mBandAddress = bandInfo.getMacAddress();
        mBandClient = mBandClientManager.create(this, bandInfo);
        mBandClient.connect().registerResultCallback(mBandConnectResultCallback);
    }

    private void getInfo() {
        if (mBandClient == null || !mBandClient.isConnected()) {
            return;
        }

        String[] bandInfo = new String[2];
        bandInfo[0] = mBandName;
        bandInfo[1] = mBandAddress;

        //Broadcast Update
        Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BandUpdateStateReceiver.UPDATE_BAND_INFO, true);
        intent.putExtra(BandUpdateStateReceiver.EXTRA_BAND_INFO, bandInfo);
        this.sendBroadcast(intent);
    }

    private void startStreaming() {
        log("Starting Stream");
        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Band is not Connected");
            return;
        }

        if (mState != STATE_STREAMING) {
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
                bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
                bandSensorManager.registerContactEventListener(mBandContactListener);
                bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
                bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
                mState = STATE_STREAMING;
                updateNotification("STREAMING");
            } catch (BandException | InvalidBandVersionException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopStreaming() {
        if (mBandClient == null) {
            return;
        }

        if (mState == STATE_STREAMING) {
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
                bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
                bandSensorManager.unregisterContactEventListener(mBandContactListener);
                bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                mState = STATE_CONNECTED;
                updateNotification("CONNECTED");
            } catch (BandIOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void enterDynamicBlackout() {
        if (mBandClient.isConnected()) {
            updateNotification("Band is not being worn");
        }
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
        try {
            bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
            bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
            bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
            bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
            bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
            bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
        } catch (BandIOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void resumeFromDynamicBlackout() {
        if (mBandClient.isConnected()) {
            updateNotification("STREAMING");
        }
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
        try {
            bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
            bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
            bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
            bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
            bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
            bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
        } catch (BandException | InvalidBandVersionException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (mBandClientManager == null) {
            log("Disconnect Failed (Band Client Manager not Initialized)");
            return;
        }

        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Disconnect Failed (Band is not Connected)");
            stopSelf();
            return;
        }

        try {
            mBandClient.getNotificationManager().vibrate(VibrationType.RAMP_DOWN);
        } catch (BandException e) {
            e.printStackTrace();
        }

        if (mState == STATE_STREAMING) {
            stopStreaming();
        }

        mBandClient.disconnect().registerResultCallback(mBandDisconnectResultCallback, 10, TimeUnit.SECONDS);
    }

    private void updateNotification(String status) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle("WBL Band Service is Running")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentText("Touch to Disconnect")
                .setSubText("Band Status: " + status)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
    */

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}