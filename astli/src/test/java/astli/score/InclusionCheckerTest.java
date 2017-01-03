package astli.score;

import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static astli.testutils.TestUtils.genFingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class InclusionCheckerTest {
    
    @Test
    public void testInclusionSamePackages() {
        InclusionChecker ic = new InclusionChecker();
        
        PackageHierarchy a = new PackageHierarchy("pckg a");
        Map<String, Fingerprint> class1prints = new HashMap<>();
        class1prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class1prints.put("method2", genFingerprint("J:V", 4, 5, 6));
        class1prints.put("method3", genFingerprint("K:V", 7, 8, 9));
        a.addMethods("class1", class1prints);
        
        PackageHierarchy b = new PackageHierarchy("pckg b");
        Map<String, Fingerprint> class2prints = new HashMap<>();
        class2prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class2prints.put("method2", genFingerprint("J:V", 4, 5, 6));
        class2prints.put("method3", genFingerprint("K:V", 7, 8, 9));
        b.addMethods("class2", class2prints);
        
        assert(ic.checkInclusion(a, b).packageAIsIncludedInB());
        assert(ic.checkInclusion(b, a).packageAIsIncludedInB());
    }
    
    @Test
    public void testInclusionSubPackage() {
        InclusionChecker ic = new InclusionChecker();
        
        PackageHierarchy a = new PackageHierarchy("pckg a");
        Map<String, Fingerprint> class1prints = new HashMap<>();
        class1prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        a.addMethods("class1", class1prints);
        
        PackageHierarchy b = new PackageHierarchy("pckg b");
        Map<String, Fingerprint> class2prints = new HashMap<>();
        class2prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        class2prints.put("method2", genFingerprint("J:V", 4, 5, 6));
        class2prints.put("method3", genFingerprint("K:V", 7, 8, 9));
        b.addMethods("class2", class2prints);
        
        assert( ic.checkInclusion(a, b).packageAIsIncludedInB());
        assert(!ic.checkInclusion(b, a).packageAIsIncludedInB());
    }
    
     @Test
    public void testInclusionDifferentPackages() {
        InclusionChecker ic = new InclusionChecker();
        
        PackageHierarchy a = new PackageHierarchy("pckg a");
        Map<String, Fingerprint> class1prints = new HashMap<>();
        class1prints.put("method1", genFingerprint("I:V", 1, 2, 3));
        a.addMethods("class1", class1prints);
        
        PackageHierarchy b = new PackageHierarchy("pckg b");
        Map<String, Fingerprint> class2prints = new HashMap<>();
        class2prints.put("method2", genFingerprint("J:V", 4, 5, 6));
        class2prints.put("method3", genFingerprint("K:V", 7, 8, 9));
        b.addMethods("class2", class2prints);
        
        assert(!ic.checkInclusion(a, b).packageAIsIncludedInB());
        assert(!ic.checkInclusion(b, a).packageAIsIncludedInB());
    }
}
