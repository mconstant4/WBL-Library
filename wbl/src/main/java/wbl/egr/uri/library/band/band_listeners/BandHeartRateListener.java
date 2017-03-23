package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;
import android.util.Log;

import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateQuality;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.library.band.BandApplication;
import wbl.egr.uri.library.io.services.DataLogService;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandHeartRateListener implements BandHeartRateEventListener {
    private static final String HEADER = "Date,Time,Heart Rate (BPM),Quality";

    private Context mContext;

    public BandHeartRateListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
        Log.d("HR Listener", "HR Data Received");
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandHeartRateEvent.getHeartRate() + "," +
                bandHeartRateEvent.getQuality();
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "/hr.csv"), data, HEADER);

        if (bandHeartRateEvent.getHeartRate() > 100 && bandHeartRateEvent.getQuality().name().equals(HeartRateQuality.LOCKED.name())) {
           // AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_TRIGGER);
        }
    }
}
