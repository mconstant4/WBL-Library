package wbl.egr.uri.library.band.exceptions;

import wbl.egr.uri.library.band.enums.BandState;

/**
 * Created by root on 3/29/17.
 */

public class BandInvalidStateException extends Exception {
    private BandState mBandState;

    public BandInvalidStateException() {
        //mBandState = bandState;
    }

    public BandState getBandState() {
        return mBandState;
    }
}
