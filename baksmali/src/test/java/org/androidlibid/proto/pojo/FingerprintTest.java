package org.androidlibid.proto.pojo;

import org.androidlibid.proto.ao.FingerprintEntity;
import org.androidlibid.proto.utils.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintTest {

    private static final Logger LOGGER = LogManager.getLogger(FingerprintTest.class);
    
    @Test
    public void testFingerprint() {
                
        Fingerprint f = createFingerprint(0, 0, 0);
        
        assert(f.getFeatureCount(0) == 0);
        f.incrementFeature(0);
        f.incrementFeature(0);
        assert(f.getFeatureCount(0) == 2);
        
        assert(f.getFeatureCount(1) == 0);
        f.incrementFeature(1);
        assert(f.getFeatureCount(1) == 1);
        f.incrementFeature(1);
        assert(f.getFeatureCount(1) == 2);
        
        assert(f.getFeatureCount(2) == 0);
        f.incrementFeature(2);
        assert(f.getFeatureCount(2) == 1);
        f.incrementFeature(2);
        assert(f.getFeatureCount(2) == 2);
        
    }

    @Test 
    public void testSimilarityBetweenEqualVectors() {
        Fingerprint f1 = createFingerprint(1, 1);
        Fingerprint f2 = createFingerprint(1, 1);
        double similarity = f1.getNonCommutativeSimilarityScoreToFingerprint(f2);
        assert(doubleEquals(similarity, 2));
    }
    
    @Test 
    public void testSimilarityBetweenCloseVectors() {
        Fingerprint f1 = createFingerprint(10, 11);
        Fingerprint f2 = createFingerprint(11, 12);
        double similarity = f1.getNonCommutativeSimilarityScoreToFingerprint(f2);
        assert(doubleEquals(similarity, 19));
    }
    
    @Test 
    public void testSimilarityBetweenDistantVectors() {
        Fingerprint f1 = createFingerprint( 1,  1);
        Fingerprint f2 = createFingerprint(20, 30);
        double similarity = f1.getNonCommutativeSimilarityScoreToFingerprint(f2);
        assert(doubleEquals(similarity, 0));
    }
    
    @Test 
    public void testNonCommutativeSimilarity() {
        Fingerprint f1 = createFingerprint(  49,35);
        Fingerprint f2 = createFingerprint(  1,  1);
        double similarity1 = f1.getNonCommutativeSimilarityScoreToFingerprint(f2);
        double similarity2 = f2.getNonCommutativeSimilarityScoreToFingerprint(f1);
        assert(similarity1 > similarity2);
        assert(doubleEquals(0, similarity2));
    }
    
    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
    
    private Fingerprint createFingerprint(int... values) {
        FingerprintEntity e = Mockito.mock(FingerprintEntity.class);
        Mockito.when(e.getName()).thenReturn("");
        Mockito.when(e.getSignature()).thenReturn("");
        Mockito.when(e.getVector()).thenReturn(ArrayUtils.truncateIntToLEByteArray(values));
        return new Fingerprint(e);
    }
}
