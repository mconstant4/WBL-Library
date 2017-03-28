package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;

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

public class BandDistanceListener implements BandDistanceEventListener {
    private static final String HEADER = "Date,Time,Motion Type,Speed (cm/s),Pace (ms/m)";

    private Context mContext;

    public BandDistanceListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandDistanceEvent.getMotionType().name() + "," +
                bandDistanceEvent.getSpeed() + "," +
                bandDistanceEvent.getPace() + ",";
        try {
            data += bandDistanceEvent.getDistanceToday();
        } catch (InvalidBandVersionException e) {
            e.printStackTrace();
        }
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "distance.csv"), data, HEADER);
    }
}
