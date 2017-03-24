package wbl.egr.uri.library.band.exceptions;

/**
 * Created by mconstant on 3/22/17.
 */

public class BandConnectionFailedException extends Exception {
    public BandConnectionFailedException() {
        super("Failed to Connect to Band");
    }
}
