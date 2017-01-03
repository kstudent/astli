package astli.find;

import astli.db.EntityService;
import astli.pojo.PackageHierarchy;
import java.sql.SQLException;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FindByNameTest {
    
    @Test
    public void testFind() throws SQLException {
        EntityService service = mock(EntityService.class);

        FindByName finder = new FindByName(service);

        PackageHierarchy h = mock(PackageHierarchy.class);
        when(h.getName()).thenReturn("hierarchy1");
        
        finder.findCandidates(h);
        
        verify(service).findPackagesByName(eq("hierarchy1"));
    }
    
    
}
