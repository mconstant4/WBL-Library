package wbl.egr.uri.library.band.enums;

/**
 * Created by root on 3/29/17.
 */

public enum BandState {
    STARTING("starting"),
    DISCONNECTING("disconnecting"),
    DISCONNECTED("disconnected"),
    CONNECTING("connecting"),
    CONNECTED("connected"),
    STREAMING("streaming"),
    NOT_WORN("not_worn"),
    PAUSED("paused");

    private String mState;

    BandState(String state) {
        mState = state;
    }

    public String getState() {
        return mState;
    }
}
