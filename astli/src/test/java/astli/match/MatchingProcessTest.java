package astli.match;

import astli.db.Clazz;
import astli.db.Library;
import astli.find.CandidateFinder;
import astli.pojo.Match;
import astli.pojo.PackageHierarchy;
import astli.score.PackageMatcher;
import astli.db.Package;
import astli.testutils.TestUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchingProcessTest {

    @Test
    public void testDefault() {
        
        PackageMatcher matcher = mock(PackageMatcher.class);
        CandidateFinder finder = mock(CandidateFinder.class);
        PackageHierarchy apkH = mock(PackageHierarchy.class);
        List<Package> libPs = mockLibraryPackageList(); 
        when(finder.findCandidates(apkH)).thenReturn(libPs.stream());
        when(matcher.getScore(eq(apkH), any(PackageHierarchy.class))).thenReturn(1.0d);

        MatchingProcess p = new MatchingProcess(matcher, finder, 0.5);
        
        Match match = p.apply(apkH);
        
        assert(match.getApkH().equals(apkH));
        assert(match.getItems().size() == 1);
        assert(TestUtils.doubleEquals(match.getItems().get(0).getScore(), 1.0d));
    }
    
    @Test
    public void testFilterLowConfidence() {
        
        PackageMatcher matcher = mock(PackageMatcher.class);
        CandidateFinder finder = mock(CandidateFinder.class);
        PackageHierarchy apkH = mock(PackageHierarchy.class);
        List<Package> libPs = mockLibraryPackageList(); 
        when(finder.findCandidates(apkH)).thenReturn(libPs.stream());
        when(matcher.getScore(eq(apkH), any(PackageHierarchy.class))).thenReturn(0.2d);
        when(matcher.getScore(eq(apkH), eq(apkH))).thenReturn(1.0d);

        MatchingProcess p = new MatchingProcess(matcher, finder, 0.5d);
        
        Match match = p.apply(apkH);
        
        assert(match.getApkH().equals(apkH));
        assert(match.getItems().isEmpty());
    
    }
    
    private List<Package> mockLibraryPackageList() {
        List<Package> libPs = new ArrayList<>();
        Package libP = mock(Package.class);
        Library lib = mock(Library.class);
        when(lib.getName()).thenReturn("lib");
        when(libP.getName()).thenReturn("libP");
        when(libP.getLibrary()).thenReturn(lib);
        when(libP.getClazzes()).thenReturn(new Clazz[0]);
        libPs.add(libP);
        return libPs;
    }
    
}
