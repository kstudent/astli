package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.junit.Test;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcherTest {
    
    @Test 
    public void testMatchSortedFingerprint() {
        FingerprintMatcher matcher = new FingerprintMatcher();
        
        List<Fingerprint> hayStack = new ArrayList<>();
        hayStack.add(createFingerprint("h1", 1, 2, 0));
        hayStack.add(createFingerprint("h2", 1, 2, 4));
        hayStack.add(createFingerprint("h3", 1, 2, 100));
        
        Fingerprint needle = createFingerprint("n", 1,2,4);
        
        List<Fingerprint> matchedPrints = (List<Fingerprint>) matcher.matchFingerprints(hayStack, needle).getMatchesByDistance();
        
        double diffto0 = needle.getDistanceToFingerprint(matchedPrints.get(0));
        double diffto1 = needle.getDistanceToFingerprint(matchedPrints.get(1));
        double diffto2 = needle.getDistanceToFingerprint(matchedPrints.get(2));
        
        assert(diffto0 <= diffto1);
        assert(diffto1 <= diffto2);
//        assert(matchedPrints.get(0).getFeatureVector().equals(hayStack.get(1).getFeatureVector()));
//        assert(matchedPrints.get(1).getFeatureVector().equals(hayStack.get(0).getFeatureVector()));
//        assert(matchedPrints.get(2).getFeatureVector().equals(hayStack.get(2).getFeatureVector()));
        
    }
    
    @Test 
    public void testMatchEqualFingerprints() {
        
        FingerprintMatcher matcher = new FingerprintMatcher();
        
        List<Fingerprint> hayStack = new ArrayList<>();
        hayStack.add(createFingerprint("h1", 1, 2, 3));
        hayStack.add(createFingerprint("h2", 1, 2, 3));
        hayStack.add(createFingerprint("h3", 1, 2, 3));
        
        Fingerprint needle = createFingerprint("n", 1,2,4);
        
        List<Fingerprint> matchedPrints = (List<Fingerprint>) matcher.matchFingerprints(hayStack, needle).getMatchesByDistance();
        
        double diffto0 = needle.getDistanceToFingerprint(matchedPrints.get(0));
        double diffto1 = needle.getDistanceToFingerprint(matchedPrints.get(1));
        double diffto2 = needle.getDistanceToFingerprint(matchedPrints.get(2));
        
        assert(diffto0 == diffto1);
        assert(diffto1 == diffto2);
    }
    
    @Test 
    public void testRemoveDistantNeedlees() throws SQLException {
        FingerprintMatcher matcher = new FingerprintMatcher(.5);
        
        List<Fingerprint> hayStack = new ArrayList<>();
        hayStack.add(createFingerprint("h1", 11));
        hayStack.add(createFingerprint("h2", 10));
        hayStack.add(createFingerprint("h3",  9));
        
        Fingerprint needle = createFingerprint("h1", 20);
        
        List<Fingerprint> matchedPrints = (List<Fingerprint>) matcher.matchFingerprints(hayStack, needle).getMatchesByDistance();
        
        assert(matchedPrints.size()  == 2);
//        assert(matchedPrints.get(0).getFeatureVector().equals(hayStack.get(0).getFeatureVector()));
//        assert(matchedPrints.get(1).getFeatureVector().equals(hayStack.get(1).getFeatureVector()));
        
    }
    
    @Test 
    public void testMatchByNameFound() throws SQLException {
        FingerprintMatcher matcher = new FingerprintMatcher();
        
        List<Fingerprint> hayStack = new ArrayList<>();
        hayStack.add(createFingerprint("NameOfFingerprint", 11));
        hayStack.add(createFingerprint("NameOfAnotherFingerprint", 11));
        
        Fingerprint needle = createFingerprint("NameOfFingerprint", 70);
        
        Result result = matcher.matchFingerprints(hayStack, needle);
        
        assert(result.getMatchByName().getName().equals("NameOfFingerprint"));
    }
    
    private Fingerprint createFingerprint(String name, int... values) {
        Fingerprint print = new Fingerprint(name);
        print.setFeatureValues(values);
        return print;
    }
    
}
