package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;

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

public class BandBarometerListener implements BandBarometerEventListener {
    private static final String HEADER = "Date,Time,Air Pressure (Hectopascals),Temperature (Celsius)";

    private Context mContext;

    public BandBarometerListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandBarometerChanged(BandBarometerEvent bandBarometerEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandBarometerEvent.getAirPressure() + "," +
                bandBarometerEvent.getTemperature();
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "/bar.csv"), data, HEADER);
    }
}
