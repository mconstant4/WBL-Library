package wbl.egr.uri.library.band.models;

/**
 * Created by mconstant on 3/29/17.
 */

public class BandModel {
    private boolean mAutoStream;
    private boolean mPeriodic;
    private String[] mSensorsToStream;

    public BandModel() {
        mSensorsToStream = SensorModel.DEFAULT_SENSORS;
        mAutoStream = false;
        mPeriodic = false;
    }

    public BandModel(String[] sensorsToStream) {
        mSensorsToStream = sensorsToStream;
        mAutoStream = false;
        mPeriodic = false;
    }

    public BandModel(String[] sensorsToStream, boolean periodic) {
        mSensorsToStream = sensorsToStream;
        mPeriodic = periodic;
        mAutoStream = false;
    }

    public BandModel(String[] sensorsToStream, boolean periodic, boolean autoStream) {
        mAutoStream = autoStream;
        mPeriodic = periodic;
        mSensorsToStream = sensorsToStream;
    }

    public boolean isAutoStream() {
        return mAutoStream;
    }

    public void setAutoStream(boolean autoStream) {
        mAutoStream = autoStream;
    }

    public boolean isPeriodic() {
        return mPeriodic;
    }

    public void setPeriodic(boolean periodic) {
        mPeriodic = periodic;
    }

    public String[] getSensorsToStream() {
        return mSensorsToStream;
    }

    public void setSensorsToStream(String[] sensorsToStream) {
        mSensorsToStream = sensorsToStream;
    }
}
