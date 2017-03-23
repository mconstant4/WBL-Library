package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;

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

public class BandSkinTemperatureListener implements BandSkinTemperatureEventListener {
    private static final String HEADER = "Date,Time,Temperature (Celsius)";

    private Context mContext;

    public BandSkinTemperatureListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandSkinTemperatureEvent.getTemperature();
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "temp.csv"), data, HEADER);
    }
}
