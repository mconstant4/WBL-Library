package wbl.egr.uri.library.band.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.icu.lang.UProperty;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
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
import wbl.egr.uri.library.band.enums.BandActions;
import wbl.egr.uri.library.band.enums.BandState;
import wbl.egr.uri.library.band.models.BandModel;
import wbl.egr.uri.library.band.models.SensorModel;
import wbl.egr.uri.library.band.receivers.BandContactStateReceiver;
import wbl.egr.uri.library.band.receivers.BandInfoReceiver;
import wbl.egr.uri.library.band.receivers.BandStateUpdateReceiver;

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
 * Created by root on 3/29/17.
 */

public class BandConnectionService extends Service {
    private static final String EXTRA_ACTION = "wbl.band.extra_action";
    private static final String EXTRA_AUTO_STREAM = "wbl.band.extra_auto_stream";
    private static final String EXTRA_SET_PERIODIC = "wbl.band.extra_set_periodic";
    private static final String EXTRA_SENSORS = "wbl.band.extra_sensors";

    public static void connect(Context context, BandModel bandModel) {
        Intent intent = new Intent(context, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.CONNECT);
        intent.putExtra(EXTRA_AUTO_STREAM, bandModel.isAutoStream());
        intent.putExtra(EXTRA_SET_PERIODIC, bandModel.isPeriodic());
        intent.putExtra(EXTRA_SENSORS, bandModel.getSensorsToStream());
        context.startService(intent);
    }

    public static void getInfo(Context context) {
        Intent intent = new Intent(context, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.GET_INFO);
        context.startService(intent);
    }

    public static void startStream(Context context) {
        Intent intent = new Intent(context, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.START_STREAM);
        context.startService(intent);
    }

    public static void stopStream(Context context) {
        Intent intent = new Intent(context, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.STOP_STREAM);
        context.startService(intent);
    }

    public static void disconnect(Context context) {
        Intent intent = new Intent(context, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.DISCONNECT);
        context.startService(intent);
    }

    private BandResultCallback<ConnectionState> mConnectionStateBandResultCallback =
            new BandResultCallback<ConnectionState>() {
                @Override
                public void onResult(ConnectionState connectionState, Throwable throwable) {
                    if (connectionState == ConnectionState.CONNECTED) {
                        log("Successfully Connected to " + mBandClientManager.getPairedBands()[0].getName());
                        updateState(BandState.CONNECTED);
                        mBandClient.registerConnectionCallback(mBandConnectionCallback);
                        if (mAutoStream) {
                            startStream();
                        }
                    } else {
                        log("Error Connecting to Band");
                        updateState(BandState.DISCONNECTED);
                    }
                }
            };
    private BandResultCallback<Void> mBandResultCallback = new BandResultCallback<Void>() {
        @Override
        public void onResult(Void aVoid, Throwable throwable) {
            log("Band Disconnected");
            mBandClient.unregisterConnectionCallback();
            updateState(BandState.DISCONNECTED);
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
    private BandContactStateReceiver mBandContactStateReceiver = new BandContactStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(BandContactStateReceiver.BAND_STATE, false)) {
                // Resume Recordings
                returnFromDynamicBlackout();
            } else {
                // Dynamic Blackout
                enterDynamicBlackout();
            }
        }
    };
    private Handler mToggleHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mBandState == BandState.STREAMING || mBandState == BandState.NOT_WORN) {
                log("Pausing Stream...");
                try {
                    mBandClient.getSensorManager().unregisterAllListeners();
                    log("Stream Paused");
                } catch (BandIOException e) {
                    e.printStackTrace();
                    log("Could not Pause Stream");
                }
                updateState(BandState.PAUSED);
                setDelayedToggle();
            } else if (mBandState == BandState.CONNECTED || mBandState == BandState.PAUSED) {
                log("Resuming Stream...");
                if (startStream()) {
                    log("Streaming Resumed");
                } else {
                    log("Streaming Failed!");
                }
            }

            return false;
        }
    });

    private final int NOTIFICATION_ID = 34;

    private BandClientManager mBandClientManager;
    private BandClient mBandClient;
    private BandState mBandState;
    private Context mContext;
    private boolean mAutoStream;
    private boolean mPeriodic;
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

    @Override
    public void onCreate() {
        super.onCreate();

        log("Service Created");

        mContext = this;
        mBandClientManager = BandClientManager.getInstance();
        mBandState = BandState.STARTING;
        mSensorsToStream = SensorModel.DEFAULT_SENSORS;
        mAutoStream = false;
        mPeriodic = false;

        startForeground(NOTIFICATION_ID, getNotification(BandState.STARTING.getState()));

        registerReceiver(mBandContactStateReceiver, BandContactStateReceiver.INTENT_FILTER);

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !intent.hasExtra(EXTRA_ACTION)) {
            log("Ignoring Action (Invalid Intent Received)");
            return START_STICKY;
        }

        switch ((BandActions) intent.getSerializableExtra(EXTRA_ACTION)) {
            case CONNECT:
                log("Connecting to Band...");
                mAutoStream = intent.getBooleanExtra(EXTRA_AUTO_STREAM, false);
                mPeriodic = intent.getBooleanExtra(EXTRA_SET_PERIODIC, false);
                if (connect(intent.getStringArrayExtra(EXTRA_SENSORS))) {
                    updateState(BandState.CONNECTING);
                } else {
                    log("Connect Failed!");
                }
                break;
            case GET_INFO:
                if (    mBandState == BandState.CONNECTED   ||
                        mBandState == BandState.STREAMING   ||
                        mBandState == BandState.NOT_WORN    ||
                        mBandState == BandState.PAUSED) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String[] bandInfo = new String[4];
                            bandInfo[0] = mBandClientManager.getPairedBands()[0].getName();
                            bandInfo[1] = mBandClientManager.getPairedBands()[0].getMacAddress();
                            try {
                                bandInfo[2] = mBandClient.getFirmwareVersion().await();
                                bandInfo[3] = mBandClient.getHardwareVersion().await();
                            } catch (BandException | InterruptedException e) {
                                e.printStackTrace();

                                bandInfo[2] = null;
                                bandInfo[3] = null;
                            } finally {
                                Intent infoIntent = new Intent(BandInfoReceiver.INTENT_FILTER.getAction(0));
                                infoIntent.putExtra(BandInfoReceiver.EXTRA_INFO, bandInfo);
                                sendBroadcast(infoIntent);
                            }
                        }
                    }).start();
                }
                break;
            case START_STREAM:
                log("Starting Stream...");
                if (startStream()) {
                    updateState(BandState.STREAMING);
                } else {
                    log("Start Stream Failed!");
                    updateState(BandState.CONNECTED);
                }
                break;
            case STOP_STREAM:
                log("Stopping Stream...");
                if (stopStream()) {
                    updateState(BandState.CONNECTED);
                } else {
                    log("Stop Stream Failed!");
                }
                break;
            case DISCONNECT:
                log("Disconnecting...");
                if (mBandState == BandState.CONNECTED) {
                    updateState(BandState.DISCONNECTING);
                    if (!disconnect()) {
                        updateState(BandState.CONNECTED);
                        log("Disconnect Failed!");
                    }
                } else {
                    log("Disconnect Failed - Invalid State (" + mBandState.getState() + ")");
                }
                break;
            case STOP:
                if (mBandState == BandState.STREAMING || mBandState == BandState.NOT_WORN || mBandState == BandState.PAUSED) {
                    stopStream();
                }

                if (mBandState == BandState.CONNECTED) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                updateState(BandState.DISCONNECTING);
                                disconnect();
                                Thread.sleep(250);
                                stopSelf();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {

                            }
                        }
                    }).start();
                } else {
                    stopSelf();
                }

                break;
            default:
                log("Ignoring Action (Invalid Action Received");
                return START_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        unregisterReceiver(mBandContactStateReceiver);
        log("Service Destroyed");
        super.onDestroy();
    }

    private boolean connect(String[] sensors) {
        if (mBandState != BandState.DISCONNECTED && mBandState != BandState.STARTING) {
            log("WARNING (Invalid State - " + mBandState.getState() + ")");
            return false;
        }

        BandInfo[] pairedBands = mBandClientManager.getPairedBands();
        if (pairedBands == null || pairedBands.length < 1) {
            log("WARNING (No Paired Bands)");
            return false;
        }

        mSensorsToStream = sensors;

        mBandClient = mBandClientManager.create(mContext, pairedBands[0]);
        mBandClient.connect().registerResultCallback(mConnectionStateBandResultCallback);

        return true;
    }

    private boolean startStream() {
        if (mBandState == BandState.CONNECTED || mBandState == BandState.PAUSED) {
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();

            if (mPeriodic) {
                log("Starting Periodic Streaming...");
            } else {
                log("Starting Streaming...");
            }

            try {
                if (mSensorsToStream == null || mSensorsToStream.length == 0) {
                    log("WARNING (No Sensors Selected)");
                    return false;
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
                                log("Warning (Invalid Sensor Name - " + sensorName + ")");
                                break;
                        }
                    }
                }
            } catch (BandException | InvalidBandVersionException e) {
                e.printStackTrace();
                return false;
            } finally {
                updateState(BandState.STREAMING);
            }
        }

        if (mPeriodic) {
            setDelayedToggle();
        }

        log("Streaming Started");
        return true;
    }

    private boolean stopStream() {
        if (mBandState == BandState.STREAMING || mBandState == BandState.NOT_WORN) {
            log("Stopping Stream...");
            try {
                mBandClient.getSensorManager().unregisterAllListeners();
                updateState(BandState.CONNECTED);
                mToggleHandler.removeMessages(3);
            } catch (BandIOException e) {
                e.printStackTrace();
                return false;
            }
        } else if (mBandState == BandState.PAUSED) {
            log("Streaming Paused - Stopping Delayed Toggle");
            updateState(BandState.CONNECTED);
            mToggleHandler.removeMessages(3);
        }

        return true;
    }

    private boolean disconnect() {
        if (mBandState == BandState.DISCONNECTING) {
            try {
                mBandClient.disconnect().registerResultCallback(mBandResultCallback);
            } catch (NullPointerException e) {
                e.printStackTrace();
                updateState(BandState.DISCONNECTED);
            }
            return true;
        } else {
            log("WARNING (Invalid State - " + mBandState.getState() + ")");
            return false;
        }
    }

    private void returnFromDynamicBlackout() {
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();

        log("Leaving Dynamic Blackout...");

        try {
            if (mSensorsToStream == null || mSensorsToStream.length == 0) {
                log("WARNING (No Sensors Selected)");
                return;
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
                            log("Warning (Invalid Sensor Name - " + sensorName + ")");
                            break;
                    }
                }
            }
        } catch (BandException | InvalidBandVersionException e) {
            e.printStackTrace();
        } finally {
            updateState(BandState.STREAMING);
        }
    }

    private void enterDynamicBlackout() {
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();

        log("Entering Dynamic Blackout...");

        try {
            if (mSensorsToStream == null || mSensorsToStream.length == 0) {
                log("WARNING (No Sensors Selected)");
                return;
            } else {
                for (String sensorName : mSensorsToStream) {
                    switch (sensorName) {
                        case SENSOR_ACCELEROMETER:
                            bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
                            break;
                        case SENSOR_ALTIMETER:
                            bandSensorManager.unregisterAltimeterEventListener(mBandAltimeterListener);
                            break;
                        case SENSOR_AMBIENT_LIGHT:
                            bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
                            break;
                        case SENSOR_BAROMETER:
                            bandSensorManager.unregisterBarometerEventListener(mBandBarometerListener);
                            break;
                        case SENSOR_CALORIES:
                            bandSensorManager.unregisterCaloriesEventListener(mBandCaloriesListener);
                            break;
                        case SENSOR_CONTACT:

                            break;
                        case SENSOR_DISTANCE:
                            bandSensorManager.unregisterDistanceEventListener(mBandDistanceListener);
                            break;
                        case SENSOR_GSR:
                            bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                            break;
                        case SENSOR_GYROSCOPE:
                            bandSensorManager.unregisterGyroscopeEventListener(mBandGyroscopeListener);
                            break;
                        case SENSOR_HEART_RATE:
                            bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                            break;
                        case SENSOR_PEDOMETER:
                            bandSensorManager.unregisterPedometerEventListener(mBandPedometerListener);
                            break;
                        case SENSOR_RR_INTERVAL:
                            bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                            break;
                        case SENSOR_SKIN_TEMPERATURE:
                            bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                            break;
                        case SENSOR_UV:
                            bandSensorManager.unregisterUVEventListener(mBandUvListener);
                            break;
                        default:
                            log("Warning (Invalid Sensor Name - " + sensorName + ")");
                            break;
                    }
                }
            }
        } catch (BandException e) {
            e.printStackTrace();
        } finally {
            updateState(BandState.NOT_WORN);
        }
    }

    private void setDelayedToggle() {
        mToggleHandler.sendEmptyMessageDelayed(3, 3 * 60 * 1000);
    }

    private void updateState(BandState bandState) {
        mBandState = bandState;

        updateNotification(bandState.getState());

        Intent intent = new Intent(BandStateUpdateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BandStateUpdateReceiver.EXTRA_STATE, bandState);
        sendBroadcast(intent);
    }

    private Notification getNotification(String status) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);
        notificationBuilder.setContentTitle("EAR is Active");
        notificationBuilder.setContentText("Band Status: " + status);
        notificationBuilder.setSmallIcon(android.R.drawable.presence_online);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setOngoing(true);

        Intent intent = new Intent(mContext, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.STOP);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 347, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        return notificationBuilder.build();
    }

    private void updateNotification(String status) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);
        notificationBuilder.setContentTitle("EAR is Active");
        notificationBuilder.setContentText("Band Status: " + status);
        notificationBuilder.setSmallIcon(android.R.drawable.presence_online);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setOngoing(true);

        Intent intent = new Intent(mContext, BandConnectionService.class);
        intent.putExtra(EXTRA_ACTION, BandActions.STOP);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 347, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void log(String message) {
        Log.d("Band Connection Service", message);
    }
}
