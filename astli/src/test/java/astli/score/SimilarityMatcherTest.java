package astli.score;

import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static astli.testutils.TestUtils.genFingerprint;
import static astli.testutils.TestUtils.doubleEquals;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class SimilarityMatcherTest {
    
    @Test
    public void testScoreEqualPackages() {
        SimilarityMatcher m = new SimilarityMatcher();
        
        PackageHierarchy a = new PackageHierarchy("pckg a");
        Map<String, Fingerprint> class1prints = new HashMap<>();
        class1prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class1prints.put("method2", genFingerprint("I:V", 4, 5, 6));
        class1prints.put("method3", genFingerprint("I:V", 7, 8, 9));
        a.addMethods("class1", class1prints);
        
        PackageHierarchy b = new PackageHierarchy("pckg b");
        Map<String, Fingerprint> class2prints = new HashMap<>();
        class2prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class2prints.put("method2", genFingerprint("I:V", 4, 5, 6));
        class2prints.put("method3", genFingerprint("I:V", 7, 8, 9));
        b.addMethods("class2", class2prints);
        
        double maxScore = m.getScore(a, a);
        double score = m.getScore(a, b);
        
        assert(doubleEquals(score / maxScore, 1.0d));
    }
    
    @Test
    public void testScoreDifferentPackages() {
        SimilarityMatcher m = new SimilarityMatcher();
        
        PackageHierarchy a = new PackageHierarchy("pckg a");
        Map<String, Fingerprint> class1prints = new HashMap<>();
        class1prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class1prints.put("method2", genFingerprint("B:V", 4, 5, 6));
        class1prints.put("method3", genFingerprint("Z:V", 7, 8, 9));
        a.addMethods("class1", class1prints);
        
        PackageHierarchy b = new PackageHierarchy("pckg b");
        Map<String, Fingerprint> class2prints = new HashMap<>();
        class2prints.put("method1", genFingerprint("I:V", 1, 2, 5));
        class2prints.put("method2", genFingerprint("B:V", 4, 2, 6));
        class2prints.put("method3", genFingerprint("Z:V", 0, 8, 9));
        b.addMethods("class2", class2prints);
        
        double maxScore = m.getScore(a, a);
        double score = m.getScore(a, b);
        
        assert(score / maxScore < 1.0d);
    }
    
    @Test
    public void testScoreTotallyDifferentPackages() {
        SimilarityMatcher m = new SimilarityMatcher();
        
        PackageHierarchy a = new PackageHierarchy("pckg a");
        Map<String, Fingerprint> class1prints = new HashMap<>();
        class1prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class1prints.put("method2", genFingerprint("J:V", 4, 5, 6));
        class1prints.put("method3", genFingerprint("K:V", 7, 8, 9));
        a.addMethods("class1", class1prints);
        
        PackageHierarchy b = new PackageHierarchy("pckg b");
        Map<String, Fingerprint> class2prints = new HashMap<>();
        class2prints.put("method1", genFingerprint("L:V", 1, 2, 5));
        class2prints.put("method2", genFingerprint("M:V", 4, 2, 6));
        class2prints.put("method3", genFingerprint("N:V", 0, 8, 9));
        b.addMethods("class2", class2prints);
        
        double maxScore = m.getScore(a, a);
        double score = m.getScore(a, b);
        
        assert(doubleEquals(score / maxScore, 0.0d));
    }
}
