package wbl.egr.uri.library.band.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.microsoft.band.notifications.VibrationType;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.GsrSampleRate;
import com.microsoft.band.sensors.SampleRate;

import java.lang.ref.WeakReference;

import wbl.egr.uri.library.band.band_listeners.BandAccelerometerListener;
import wbl.egr.uri.library.band.band_listeners.BandAltimeterListener;
import wbl.egr.uri.library.band.band_listeners.BandAmbientLightListener;
import wbl.egr.uri.library.band.band_listeners.BandBarometerListener;
import wbl.egr.uri.library.band.band_listeners.BandCaloriesListener;
import wbl.egr.uri.library.band.band_listeners.BandContactListener;
import wbl.egr.uri.library.band.band_listeners.BandDistanceListener;
import wbl.egr.uri.library.band.band_listeners.BandGsrListener;
import wbl.egr.uri.library.band.band_listeners.BandGyroscopeListener;
import wbl.egr.uri.library.band.band_listeners.BandHeartRateListener;
import wbl.egr.uri.library.band.band_listeners.BandPedometerListener;
import wbl.egr.uri.library.band.band_listeners.BandRRIntervalListener;
import wbl.egr.uri.library.band.band_listeners.BandSkinTemperatureListener;
import wbl.egr.uri.library.band.band_listeners.BandUvListener;
import wbl.egr.uri.library.band.receivers.BandContactStateReceiver;
import wbl.egr.uri.library.band.receivers.BandInfoReceiver;
import wbl.egr.uri.library.band.receivers.BandUpdateStateReceiver;
import wbl.egr.uri.library.band.receivers.NotificationReceiver;

import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_ACCELEROMETER;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_ALTIMETER;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_AMBIENT_LIGHT;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_BAROMETER;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_CALORIES;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_CONTACT;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_DISTANCE;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_GSR;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_GYROSCOPE;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_HEART_RATE;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_PEDOMETER;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_RR_INTERVAL;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_SKIN_TEMPERATURE;
import static wbl.egr.uri.library.band.models.SensorModel.SENSOR_UV;

/**
 * Created by root on 3/22/17.
 *
 */

public class BandConnectionJobService extends JobService {
    private static final String KEY_ACTION = "uri.egr.wbl.library.band_action";
    private static final String KEY_SET_PERIODIC = "uri.egr.wbl.library.band_set_periodic";
    private static final String KEY_SENSORS_TO_STREAM = "uri.egr.wbl.library.band_sensors_to_stream";
    private static final String KEY_ENABLE_HAPTIC_FEEDBACK = "uri.egr.wbl.library.band_enable_haptic_feedback";

    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_DISCONNECT = 1;
    private static final int ACTION_START_STREAM = 2;
    private static final int ACTION_STOP_STREAM = 3;
    private static final int ACTION_REQUEST_BAND_INFO = 4;
    private static final int ACTION_STOP_SERVICE = 5;

    public static final int STATE_CONNECTED = 0;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_STREAMING = 2;
    public static final int STATE_BAND_NOT_WORN = 3;
    public static final int STATE_BAND_OFF = 4;

    public static void connect(WeakReference<Context> context, boolean enableHapticFeedback) {
        if (context == null) {
            Log.d("BandConnectionService", "Connect Call Failed (Called with invalid parameters)");
            return;
        }

        if (context.get() != null) {
            JobScheduler jobScheduler = (JobScheduler) context.get().getSystemService(Context.JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt(KEY_ACTION, ACTION_CONNECT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                bundle.putBoolean(KEY_ENABLE_HAPTIC_FEEDBACK, enableHapticFeedback);
            }

            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Connect Call Failed (Error Scheduling Job)");
            }
        }
    }

    public static void disconnect(WeakReference<Context> context) {
        if (context == null) {
            Log.d("BandConnectionService", "Disconnect Call Failed (Called with invalid parameters)");
            return;
        }

        if (context.get() != null) {
            JobScheduler jobScheduler = (JobScheduler) context.get().getSystemService(JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt(KEY_ACTION, ACTION_DISCONNECT);

            JobInfo.Builder builder = new JobInfo.Builder(2, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(200);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Disconnect Call Failed (Error Scheduling Job)");
            }
        }
    }

    public static void startStream(WeakReference<Context> context, boolean isPeriodic, String[] sensorsToStream) {
        if (context == null) {
            Log.d("BandConnectionService", "Start Stream Call Failed (Called with invalid parameters)");
            return;
        }

        if (context.get() != null) {
            JobScheduler jobScheduler = (JobScheduler) context.get().getSystemService(JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt(KEY_ACTION, ACTION_START_STREAM);
            bundle.putStringArray(KEY_SENSORS_TO_STREAM, sensorsToStream);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (isPeriodic) {
                    bundle.putBoolean(KEY_SET_PERIODIC, true);
                } else {
                    bundle.putBoolean(KEY_SET_PERIODIC, false);
                }
            }

            JobInfo.Builder builder = new JobInfo.Builder(3, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            if (isPeriodic) {
                builder.setPeriodic(60000);
            } else {
                builder.setOverrideDeadline(10);
            }
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Start Stream Call Failed (Error Scheduling Job)");
            }
        }
    }

    public static void stopStream(WeakReference<Context> context) {
        if (context == null) {
            Log.d("BandConnectionService", "Stop Stream Call Failed (Called with invalid parameters)");
            return;
        }

        if (context.get() != null) {
            JobScheduler jobScheduler = (JobScheduler) context.get().getSystemService(JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt(KEY_ACTION, ACTION_START_STREAM);

            JobInfo.Builder builder = new JobInfo.Builder(4, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Stop Stream Call Failed (Error Scheduling Job)");
            }
        }
    }

    public static void requestBandInfo(WeakReference<Context> context) {
        if (context == null) {
            Log.d("BandConnectionService", "Request Band Info Call Failed (Called with invalid parameters)");
            return;
        }

        if (context.get() != null) {
            JobScheduler jobScheduler = (JobScheduler) context.get().getSystemService(JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt(KEY_ACTION, ACTION_REQUEST_BAND_INFO);

            JobInfo.Builder builder = new JobInfo.Builder(5, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Request Band Info Call Failed (Error Scheduling Job)");
            }
        }
    }

    public static void startService(Context context) {
        context.startService(new Intent(context, BandConnectionJobService.class));
    }

    public static void stopService(WeakReference<Context> context) {
        if (context == null) {
            Log.d("BandConnectionService", "Stop Service Call Failed (Called with invalid parameters)");
            return;
        }

        if (context.get() != null) {
            JobScheduler jobScheduler = (JobScheduler) context.get().getSystemService(Context.JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt(KEY_ACTION, ACTION_STOP_SERVICE);

            JobInfo.Builder builder = new JobInfo.Builder(8, new ComponentName(context.get().getPackageName(), BandConnectionJobService.class.getName()));
            builder.setExtras(bundle);
            builder.setOverrideDeadline(10);
            if (jobScheduler.schedule(builder.build()) <= 0) {
                Log.d("BandConnectionService", "Stop Service Call Failed (Error Scheduling Job)");
            }
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
    private BandAltimeterListener mBandAltimeterListener;
    private BandAmbientLightListener mBandAmbientLightListener;
    private BandBarometerListener mBandBarometerListener;
    private BandCaloriesListener mBandCaloriesListener;
    private BandContactListener mBandContactListener;
    private BandDistanceListener mBandDistanceListener;
    private BandGsrListener mBandGsrListener;
    private BandGyroscopeListener mBandGyroscopeListener;
    private BandHeartRateListener mBandHeartRateListener;
    private BandPedometerListener mBandPedometerListener;
    private BandRRIntervalListener mBandRRIntervalListener;
    private BandSkinTemperatureListener mBandSkinTemperatureListener;
    private BandUvListener mBandUvListener;

    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            log("handleMessage()");
            JobParameters parameters = (JobParameters) msg.obj;

            switch (parameters.getExtras().getInt(KEY_ACTION)) {
                case ACTION_CONNECT:
                    if (mBandClient != null && (mBandClient != null && mBandClient.isConnected())) {
                        log("Band already connected!");
                        break;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        mEnableHapticFeedback = parameters.getExtras().getBoolean(KEY_ENABLE_HAPTIC_FEEDBACK, false);
                    }
                    connect();
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
                case ACTION_START_STREAM:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        mIsPeriodic = parameters.getExtras().getBoolean(KEY_SET_PERIODIC, false);
                    }
                    mSensorsToStream = parameters.getExtras().getStringArray(KEY_SENSORS_TO_STREAM);

                    startStream();
                    break;
                case ACTION_STOP_STREAM:
                    stopStream();
                    break;
                case ACTION_REQUEST_BAND_INFO:
                    getBandInfo();
                    break;
                case ACTION_STOP_SERVICE:
                    updateNotification("Stopping");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            stopStream();
                            disconnect();
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            stopSelf();
                        }
                    }).start();
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
            stopStream();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate()");

        mContext = this;
        mBandClientManager = BandClientManager.getInstance();

        mBandAccelerometerListener = new BandAccelerometerListener(this);
        mBandAltimeterListener = new BandAltimeterListener(this);
        mBandAmbientLightListener = new BandAmbientLightListener(this);
        mBandBarometerListener = new BandBarometerListener(this);
        mBandCaloriesListener = new BandCaloriesListener(this);
        mBandContactListener = new BandContactListener(this);
        mBandDistanceListener = new BandDistanceListener(this);
        mBandGsrListener = new BandGsrListener(this);
        mBandGyroscopeListener = new BandGyroscopeListener(this);
        mBandHeartRateListener = new BandHeartRateListener(this);
        mBandPedometerListener = new BandPedometerListener(this);
        mBandRRIntervalListener = new BandRRIntervalListener(this);
        mBandSkinTemperatureListener = new BandSkinTemperatureListener(this);
        mBandUvListener = new BandUvListener(this);

        registerReceiver(mBandContactStateReceiver, BandContactStateReceiver.INTENT_FILTER);

        // Generate Pending Intent for Notification
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Declare as Foreground Service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("WBL Band Service is Running")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentText("Touch to Disconnect")
                .setSubText("Band Status: Initializing")
                .setContentIntent(pendingIntent)
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
        log("onDestroy()");
        unregisterReceiver(mBandContactStateReceiver);

        super.onDestroy();
    }

    private void connect() {
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
    }

    private void disconnect() {
        if (mBandClient != null && mBandClient.isConnected()) {
            log("Disconnecting from Band...");
            mBandClient.disconnect().registerResultCallback(mBandDisconnectResultCallback);
        } else {
            log("Band is not Connected");
        }
    }

    private void startStream() {
        log("Starting Stream");

        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Band is not Connected");
        } else {
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                if (mSensorsToStream == null || mSensorsToStream.length == 0) {
                    log("No Sensors Selected!");
                } else {
                    for (String sensorName : mSensorsToStream) {
                        switch (sensorName) {
                            case SENSOR_ACCELEROMETER:
                                bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
                                break;
                            case SENSOR_ALTIMETER:
                                bandSensorManager.registerAltimeterEventListener(mBandAltimeterListener);
                                break;
                            case SENSOR_AMBIENT_LIGHT:
                                bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
                                break;
                            case SENSOR_BAROMETER:
                                bandSensorManager.registerBarometerEventListener(mBandBarometerListener);
                                break;
                            case SENSOR_CALORIES:
                                bandSensorManager.registerCaloriesEventListener(mBandCaloriesListener);
                                break;
                            case SENSOR_CONTACT:
                                bandSensorManager.registerContactEventListener(mBandContactListener);
                                break;
                            case SENSOR_DISTANCE:
                                bandSensorManager.registerDistanceEventListener(mBandDistanceListener);
                                break;
                            case SENSOR_GSR:
                                bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS5000);
                                break;
                            case SENSOR_GYROSCOPE:
                                bandSensorManager.registerGyroscopeEventListener(mBandGyroscopeListener, SampleRate.MS128);
                                break;
                            case SENSOR_HEART_RATE:
                                bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
                                break;
                            case SENSOR_PEDOMETER:
                                bandSensorManager.registerPedometerEventListener(mBandPedometerListener);
                                break;
                            case SENSOR_RR_INTERVAL:
                                bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
                                break;
                            case SENSOR_SKIN_TEMPERATURE:
                                bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
                                break;
                            case SENSOR_UV:
                                bandSensorManager.registerUVEventListener(mBandUvListener);
                                break;
                            default:
                                log("Invalid Sensor Name!");
                                break;
                        }
                    }
                }
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
    }

    private void stopStream() {
        if (mBandClient == null) {
            log("Band is not Connected");
        } else {
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.unregisterAllListeners();
            } catch (BandIOException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            //Send Broadcast to BandStateUpdateReceivers
            Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
            intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_CONNECTED);
            sendBroadcast(intent);
        }
    }

    private void getBandInfo() {
        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Band is not Connected");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] bandInfo = new String[4];
                    try {
                        bandInfo[0] = mBandClient.getFirmwareVersion().await();
                        bandInfo[1] = mBandClient.getHardwareVersion().await();
                        bandInfo[2] = mBandClientManager.getPairedBands()[0].getName();
                        bandInfo[3] = mBandClientManager.getPairedBands()[0].getMacAddress();

                        Intent intent = new Intent(BandInfoReceiver.INTENT_FILTER.getAction(0));
                        intent.putExtra(BandInfoReceiver.EXTRA_INFO, bandInfo);
                        sendBroadcast(intent);
                    } catch (BandException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void vibrateBand(VibrationType vibrationType) {
        if (mBandClient == null || !mBandClient.isConnected()) {
            //Throw Exception
            log("Band is not Connected");
        } else {
            try {
                mBandClient.getNotificationManager().vibrate(vibrationType);
            } catch (BandException e) {
                e.printStackTrace();
            }
        }
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

                //Provide Haptic Feedback
                if (mEnableHapticFeedback) {
                    vibrateBand(VibrationType.RAMP_UP);
                }

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

            //Provide Haptic Feedback
            if (mEnableHapticFeedback) {
                vibrateBand(VibrationType.RAMP_DOWN);
            }

            //Send Broadcast to BandStateUpdateReceivers
            Intent intent = new Intent(BandUpdateStateReceiver.INTENT_FILTER.getAction(0));
            intent.putExtra(BandUpdateStateReceiver.EXTRA_NEW_STATE, STATE_DISCONNECTED);
            sendBroadcast(intent);

            stopSelf();
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
