package astli.testutils;

import astli.pojo.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class TestUtils {
 
    public static boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
    
    public static Fingerprint genFingerprint(String signature, int... values) {
        Fingerprint fp = new Fingerprint();
        fp.setSignature(signature);
        for(int i = 0; i < values.length; i++) {
            fp.incrementFeatureBy(i, (short)values[i]);
        }
        return fp;
    }
}
