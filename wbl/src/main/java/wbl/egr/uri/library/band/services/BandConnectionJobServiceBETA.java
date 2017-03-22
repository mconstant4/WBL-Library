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
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandConnectionCallback;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandResultCallback;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.GsrSampleRate;
import com.microsoft.band.sensors.SampleRate;

import wbl.egr.uri.library.band.band_listeners.BandAccelerometerListener;
import wbl.egr.uri.library.band.band_listeners.BandAmbientLightListener;
import wbl.egr.uri.library.band.band_listeners.BandContactListener;
import wbl.egr.uri.library.band.band_listeners.BandGsrListener;
import wbl.egr.uri.library.band.band_listeners.BandHeartRateListener;
import wbl.egr.uri.library.band.band_listeners.BandRRIntervalListener;
import wbl.egr.uri.library.band.band_listeners.BandSkinTemperatureListener;
import wbl.egr.uri.library.band.receivers.BandContactStateReceiver;
import wbl.egr.uri.library.band.receivers.BandUpdateStateReceiver;

import static wbl.egr.uri.library.band.receivers.BandContactStateReceiver.BAND_STATE;

/**
 * Created by root on 3/22/17.
 *
 */

public class BandConnectionJobServiceBETA extends JobService {
    private static final String KEY_ACTION = "uri.egr.wbl.library.band_action";
    private static final String KEY_SET_PERIODIC = "uri.egr.wbl.library.band_set_periodic";
    private static final String KEY_SENSORS_TO_STREAM = "uri.egr.wbl.library.band_sensors_to_stream";
    private static final String KEY_ENABLE_HAPTIC_FEEDBACK = "uri.egr.wbl.library.band_enable_haptic_feedback";

    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_DISCONNECT = 1;
    private static final int ACTION_START_STREAM = 2;
    private static final int ACTION_STOP_STREAM = 3;
    private static final int ACTION_REQUEST_BAND_INFO = 4;

    public static final int STATE_CONNECTED = 0;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_STREAMING = 2;
    public static final int STATE_BAND_NOT_WORN = 3;
    public static final int STATE_BAND_OFF = 4;

    public static void connect(Context context, JobScheduler jobScheduler, boolean enableHapticFeedback) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Connect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobServiceBETA.KEY_ACTION, BandConnectionJobServiceBETA.ACTION_CONNECT);

        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context.getPackageName(), BandConnectionJobServiceBETA.class.getName()));
        builder.setExtras(bundle);
        builder.setOverrideDeadline(10);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.d("BandConnectionService", "Connect Failed (Error Scheduling Job)");
        }
    }

    public static void disconnect(Context context, JobScheduler jobScheduler) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Disconnect Failed (Connect called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobServiceBETA.KEY_ACTION, BandConnectionJobServiceBETA.ACTION_DISCONNECT);

        JobInfo.Builder builder = new JobInfo.Builder(2, new ComponentName(context.getPackageName(), BandConnectionJobServiceBETA.class.getName()));
        builder.setExtras(bundle);
        builder.setOverrideDeadline(200);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.d("BandConnectionService", "Disconnect Failed (Error Scheduling Job)");
        }
    }

    public static void startStream(Context context, JobScheduler jobScheduler, boolean isPeriodic, String[] sensorsToStream) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Start Stream Failed (Called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobServiceBETA.KEY_ACTION, BandConnectionJobServiceBETA.ACTION_START_STREAM);
        bundle.putStringArray(KEY_SENSORS_TO_STREAM, sensorsToStream);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (isPeriodic) {
                bundle.putBoolean(KEY_SET_PERIODIC, true);
            } else {
                bundle.putBoolean(KEY_SET_PERIODIC, false);
            }
        }

        JobInfo.Builder builder = new JobInfo.Builder(3, new ComponentName(context.getPackageName(), BandConnectionJobServiceBETA.class.getName()));
        builder.setExtras(bundle);
        if (isPeriodic) {
            builder.setPeriodic(60000);
        } else {
            builder.setOverrideDeadline(10);
        }
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.d("BandConnectionService", "Start Stream Failed (Error Scheduling Job)");
        }
    }

    public static void stopStream(Context context, JobScheduler jobScheduler) {
        if (context == null || jobScheduler == null) {
            Log.d("BandConnectionService", "Stop Stream Failed (Called with invalid parameters)");
            return;
        }

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(BandConnectionJobServiceBETA.KEY_ACTION, BandConnectionJobServiceBETA.ACTION_START_STREAM);

        JobInfo.Builder builder = new JobInfo.Builder(4, new ComponentName(context.getPackageName(), BandConnectionJobServiceBETA.class.getName()));
        builder.setExtras(bundle);
        builder.setOverrideDeadline(10);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.d("BandConnectionService", "Stop Stream Failed (Error Scheduling Job)");
        }
    }

    private final int NOTIFICATION_ID = 437;

    private Context mContext;

    private BandClientManager mBandClientManager;
    private BandClient mBandClient;
    private boolean mIsPeriodic;
    private boolean mEnableHapticFeedback;
    private String[] mSensorsToStream;

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
            log("handleMessage()");
            JobParameters parameters = (JobParameters) msg.obj;

            switch (parameters.getExtras().getInt(KEY_ACTION)) {
                case ACTION_CONNECT:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        mEnableHapticFeedback = parameters.getExtras().getBoolean(KEY_ENABLE_HAPTIC_FEEDBACK, false);
                    }

                    BandInfo[] pairedBands = mBandClientManager.getPairedBands();
                    //For now, only 1 Band at a time is supported
                    if (pairedBands == null || pairedBands.length < 1) {
                        //Throw Exception NoPairedBands
                        log("Please pair your Band via the Microsoft Band App first");
                    } else {
                        mBandClient = mBandClientManager.create(mContext, pairedBands[0]);
                        mBandClient.connect().registerResultCallback(mBandConnectResultCallback);
                        log("Connecting to Band...");
                    }
                    break;
                case ACTION_DISCONNECT:
                    if (mBandClient != null && mBandClient.isConnected()) {
                        log("Disconnecting from Band...");
                        mBandClient.disconnect().registerResultCallback(mBandDisconnectResultCallback);
                    } else {
                        log("Band is not Connected");
                    }
                    break;
                case ACTION_START_STREAM:
                    log("Starting Stream");
                    if (mBandClient == null || !mBandClient.isConnected()) {
                        log("Band is not Connected");
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            mIsPeriodic = parameters.getExtras().getBoolean(KEY_SET_PERIODIC, false);
                        }

                        mSensorsToStream = parameters.getExtras().getStringArray(KEY_SENSORS_TO_STREAM);

                        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
                        try {
                            //Edit this
                            bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
                            bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
                            bandSensorManager.registerContactEventListener(mBandContactListener);
                            bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
                            bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
                            bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
                            bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
                        } catch (BandException | InvalidBandVersionException e) {
                            e.printStackTrace();
                        }

                        updateNotification("Streaming");

                        //Send Broadcast to BandStateUpdateReceivers
                        Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                        intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_STREAMING);
                        sendBroadcast(intent);

                        if (mIsPeriodic) {
                            mCountDownTimer.start();
                        }
                    }
                    break;
                case ACTION_STOP_STREAM:
                    if (mBandClient == null) {
                        log("Band is not Connected");
                    } else {
                        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
                        try {
                            //And this
                            bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
                            bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
                            bandSensorManager.unregisterContactEventListener(mBandContactListener);
                            bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                            bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                            bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                            bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                        } catch (BandIOException | IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                        //Send Broadcast to BandStateUpdateReceivers
                        Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                        intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_CONNECTED);
                        sendBroadcast(intent);
                    }
                    break;
                case ACTION_REQUEST_BAND_INFO:

                    break;
            }

            jobFinished(parameters, false);
            return false;
        }
    });

    private CountDownTimer mCountDownTimer = new CountDownTimer(30000, 30000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (mBandClient == null) {
                log("Band is not Connected");
            } else {
                BandSensorManager bandSensorManager = mBandClient.getSensorManager();
                try {
                    //And this
                    bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
                    bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
                    bandSensorManager.unregisterContactEventListener(mBandContactListener);
                    bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                    bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                    bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                    bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                } catch (BandIOException | IllegalArgumentException e) {
                    e.printStackTrace();
                }

                updateNotification("Connected");

                //Send Broadcast to BandStateUpdateReceivers
                Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_CONNECTED);
                sendBroadcast(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate()");

        mContext = this;
        mBandClientManager = BandClientManager.getInstance();

        mBandAccelerometerListener = new BandAccelerometerListener(this);
        mBandAmbientLightListener = new BandAmbientLightListener(this);
        mBandContactListener = new BandContactListener(this);
        mBandGsrListener = new BandGsrListener(this);
        mBandHeartRateListener = new BandHeartRateListener(this);
        mBandRRIntervalListener = new BandRRIntervalListener(this);
        mBandSkinTemperatureListener = new BandSkinTemperatureListener(this);

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
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        log("onStartJob()");

        mJobHandler.sendMessage(Message.obtain(mJobHandler, (int) Math.round(Math.random()), params));

        //True if your service needs to process the work (on a separate thread). False if there's no more work to be done for this job.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        log("onStopJob");

        //True to indicate to the JobManager whether you'd like to reschedule this job based on the retry criteria provided at job creation-time.
        //False to drop the job. Regardless of the value returned, your job must stop executing.
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy()");
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

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }

    private BandResultCallback<ConnectionState> mBandConnectResultCallback = new BandResultCallback<ConnectionState>() {
        @Override
        public void onResult(ConnectionState connectionState, Throwable throwable) {
            if (connectionState == ConnectionState.CONNECTED) {
                //Success
                log("Band is Connected");

                updateNotification("Connected");

                //Send Broadcast to BandStateUpdateReceivers
                Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_CONNECTED);
                sendBroadcast(intent);

                mBandClient.registerConnectionCallback(mBandConnectionCallback);
            } else {
                //Throw Exception BandConnectionFailed
                log("Could not connect to Band");
            }
        }
    };

    private BandConnectionCallback mBandConnectionCallback = new BandConnectionCallback() {
        @Override
        public void onStateChanged(ConnectionState connectionState) {
            switch (connectionState) {
                case BINDING:
                    log("Binding");
                    break;
                case BOUND:
                    log("Bound");
                    break;
                case CONNECTED:
                    log("Connected");

                    updateNotification("Connected");

                    //Send Broadcast to BandStateUpdateReceivers
                    Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_CONNECTED);
                    sendBroadcast(intent);
                    break;
                case UNBINDING:
                    log("Unbinding");
                    break;
                case UNBOUND:
                    log("Unbound");
                    break;
                case INVALID_SDK_VERSION:
                    log("Invalid SDK Version");
                    break;
                case DISPOSED:
                    log("Disposed");
                    break;
            }
        }
    };

    private BandResultCallback<Void> mBandDisconnectResultCallback = new BandResultCallback<Void>() {
        @Override
        public void onResult(Void aVoid, Throwable throwable) {
            log("Band Disconnected");

            updateNotification("Disconnected");

            //Send Broadcast to BandStateUpdateReceivers
            Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
            intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_DISCONNECTED);
            sendBroadcast(intent);
        }
    };

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
}
