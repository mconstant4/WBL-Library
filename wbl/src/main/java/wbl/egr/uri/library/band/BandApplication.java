package wbl.egr.uri.library.band;

import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mconstant on 3/22/17.
 */

public class BandApplication extends Application {
    public static File ROOT_DIR = getBaseFile();
    public static File getBaseFile() {
        File root;
        root = new File("/storage/sdcard1");
        if (!root.exists() || !root.canWrite()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS);
            } else {
                root = new File(Environment.getExternalStorageDirectory(), "Documents");
            }
        }
        File directory;

        directory = new File(root, ".anear");
        String date = new SimpleDateFormat("MM_dd_yyyy", Locale.US).format(new Date());
        File rootDir = new File(directory, date);
        if (!rootDir.exists()) {
            if (rootDir.mkdirs()) {
                Log.d("MAIN", "Made parent directories");
            }
        }
        return rootDir;
    }
}
