package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.library.band.BandApplication;
import wbl.egr.uri.library.io.services.DataLogService;

/**
 * Created by mconstant on 3/25/17.
 */

public class BandGyroscopeListener implements BandGyroscopeEventListener {
    private static final String HEADER = "Date,Time,Angular Velocity (degrees/sec)";

    private Context mContext;

    public BandGyroscopeListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandGyroscopeEvent.getAngularVelocityX() + "," +
                bandGyroscopeEvent.getAngularVelocityY() + "," +
                bandGyroscopeEvent.getAngularVelocityZ();
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "gyro.csv"), data, HEADER);
    }
}
