package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;

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

public class BandPedometerListener implements BandPedometerEventListener {
    private static final String HEADER = "Date,Time,Steps";

    private Context mContext;

    public BandPedometerListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + ",";
        try {
            data += bandPedometerEvent.getStepsToday();
        } catch (InvalidBandVersionException e) {
            e.printStackTrace();
        }
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "ped.csv"), data, HEADER);
    }
}
