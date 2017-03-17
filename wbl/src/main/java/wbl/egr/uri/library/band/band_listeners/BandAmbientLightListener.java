package wbl.egr.uri.library.band.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandAmbientLightListener implements BandAmbientLightEventListener {
    private static final String HEADER = "Date,Time,Brightness (LUX)";

    private Context mContext;

    public BandAmbientLightListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandAmbientLightChanged(BandAmbientLightEvent bandAmbientLightEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US).format(date);
        String data = dateString + "," + timeString + "," +
                bandAmbientLightEvent.getBrightness();
        //DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "light.csv"), data, HEADER);
    }
}
