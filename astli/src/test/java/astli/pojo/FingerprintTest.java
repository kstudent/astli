package astli.pojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mockito.Mockito;
import astli.db.Method;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintTest {

    private static final Logger LOGGER = LogManager.getLogger(FingerprintTest.class);
    
    @Test
    public void testFingerprint() {
                
        Fingerprint f = createFingerprint(0, 0, 0);
        
        assert(f.getFeatureValue(0) == 0);
        f.incrementFeature(0);
        f.incrementFeature(0);
        assert(f.getFeatureValue(0) == 2);
        
        assert(f.getFeatureValue(1) == 0);
        f.incrementFeature(1);
        assert(f.getFeatureValue(1) == 1);
        f.incrementFeature(1);
        assert(f.getFeatureValue(1) == 2);
        
        assert(f.getFeatureValue(2) == 0);
        f.incrementFeature(2);
        assert(f.getFeatureValue(2) == 1);
        f.incrementFeature(2);
        assert(f.getFeatureValue(2) == 2);
        
    }

    @Test 
    public void testSimilarityBetweenEqualVectors() {
        Fingerprint f1 = createFingerprint(1, 1);
        Fingerprint f2 = createFingerprint(1, 1);
        double similarity = f1.getSimilarityTo(f2);
        assert(doubleEquals(similarity, 2));
    }
    
    @Test 
    public void testSimilarityBetweenCloseVectors() {
        Fingerprint f1 = createFingerprint(10, 11);
        Fingerprint f2 = createFingerprint(11, 12);
        double similarity = f1.getSimilarityTo(f2);
        assert(doubleEquals(similarity, 19));
    }
    
    @Test 
    public void testSimilarityBetweenDistantVectors() {
        Fingerprint f1 = createFingerprint( 1,  1);
        Fingerprint f2 = createFingerprint(20, 30);
        double similarity = f1.getSimilarityTo(f2);
        assert(doubleEquals(similarity, 0));
    }
    
    @Test 
    public void testNonCommutativeSimilarity() {
        Fingerprint f1 = createFingerprint(  49,35);
        Fingerprint f2 = createFingerprint(  1,  1);
        double similarity1 = f1.getSimilarityTo(f2);
        double similarity2 = f2.getSimilarityTo(f1);
        assert(similarity1 > similarity2);
        assert(doubleEquals(0, similarity2));
    }
    
    @Test
    public void testToString() {
        Fingerprint f = createFingerprint(1, 2, 3);
        String fstring = f.toString();
        assert(fstring.contains("nameOfMethod"));
        assert(fstring.contains("I[:Z"));
        assert(fstring.contains("1"));
        assert(fstring.contains("2"));
        assert(fstring.contains("3"));
    }
    
    @Test
    public void testIncrementFeature() {
        Fingerprint f = createFingerprint(1, 3, 5);
        f.incrementFeature(NodeType.VRT);
        assert(f.getFeatureValue(NodeType.VRT) == 2);
    }
    
    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
    
    private Fingerprint createFingerprint(int... values) {
        Method e = Mockito.mock(Method.class);
        Mockito.when(e.getName()).thenReturn("nameOfMethod");
        Mockito.when(e.getSignature()).thenReturn("I[:Z");
        Mockito.when(e.getVector()).thenReturn(ArrayUtils.truncateIntToLEByteArray(values));
        return new Fingerprint(e);
    }
}
