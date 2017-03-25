package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;

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

public class BandUvListener implements BandUVEventListener {
    private static final String HEADER = "Date,Time,UV Index,UV Exposure";

    private Context mContext;

    public BandUvListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandUVChanged(BandUVEvent bandUVEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandUVEvent.getUVIndexLevel() + ",";
        try {
            data += bandUVEvent.getUVExposureToday();
        }catch (InvalidBandVersionException e) {
            e.printStackTrace();
        }
        DataLogService.log(mContext, new File(BandApplication.ROOT_DIR, "uv.csv"), data, HEADER);
    }
}
