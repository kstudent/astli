package astli.find;

import astli.db.Package;
import astli.pojo.PackageHierarchy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FindByNameOrNeedleTest {
    
    @Test 
    public void testFindByName() {
        
        FindByName nameF = mock(FindByName.class);
        FindByNeedle needleF = mock(FindByNeedle.class);
        FindByNameOrNeedle finder = new FindByNameOrNeedle(nameF, needleF);

        PackageHierarchy h = mock(PackageHierarchy.class);
        when(h.getName()).thenReturn("hierarchy1");
        
        List<Package> expectedMatches = new ArrayList<Package>();
        expectedMatches.add((Package) mock(Package.class));
 
        when(nameF.findCandidates(h)).thenReturn(expectedMatches.stream());
        
        Stream<Package> returnedMatches = finder.findCandidates(h);
        
        assert(returnedMatches.collect(Collectors.toList()).equals(expectedMatches));
        verify(needleF, never()).findCandidates(h);
    }
    
    @Test 
    public void testFindByNeedle() {
        
        FindByName nameF = mock(FindByName.class);
        
        FindByNeedle needleF = mock(FindByNeedle.class);
        
        FindByNameOrNeedle finder = new FindByNameOrNeedle(nameF, needleF);

        PackageHierarchy h = mock(PackageHierarchy.class);
        when(h.getName()).thenReturn("hierarchy1");
        
        List<Package> emtpyNameMatches = new ArrayList<Package>();
        when(nameF.findCandidates(h)).thenReturn(emtpyNameMatches.stream());
        
        List<Package> expectedMatches = new ArrayList<Package>();
        expectedMatches.add(mock(Package.class));
        when(needleF.findCandidates(h)).thenReturn(expectedMatches.stream());
        
        Stream<Package> returnedMatches = finder.findCandidates(h);
        
        assert(returnedMatches.collect(Collectors.toList()).equals(expectedMatches));
        verify(nameF).findCandidates(h);
        verify(needleF).findCandidates(h);
    }
}
