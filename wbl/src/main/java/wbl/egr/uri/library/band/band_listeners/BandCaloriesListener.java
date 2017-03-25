package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;

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

public class BandCaloriesListener implements BandCaloriesEventListener {
    private static final String HEADER = "Date,Time,Calories Burned (kcals)";

    private Context mContext;

    public BandCaloriesListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandCaloriesChanged(BandCaloriesEvent bandCaloriesEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = "";
                try {
                    data = dateString + "," + timeString + "," + bandCaloriesEvent.getCaloriesToday();
                } catch (InvalidBandVersionException e) {
                    e.printStackTrace();
                }
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "/cal.csv"), data, HEADER);
    }
}
