package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandAltimeterEventListener;
import com.microsoft.band.sensors.BandAmbientLightEvent;

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

public class BandAltimeterListener implements BandAltimeterEventListener {
    private static final String HEADER = "Date,Time,Rate of Ascension (cm/s),Flights Ascended (floors), Elevation Gained (cm)";

    private Context mContext;

    public BandAltimeterListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandAltimeterChanged(BandAltimeterEvent bandAltimeterEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = "";
        try {
            data = dateString + "," + timeString + "," +
                    bandAltimeterEvent.getRate() +
                    bandAltimeterEvent.getFlightsAscendedToday() +
                    bandAltimeterEvent.getTotalGainToday();
        } catch (InvalidBandVersionException e) {
            data += ",,";
            e.printStackTrace();
        }
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "alt.csv"), data, HEADER);
    }
}
