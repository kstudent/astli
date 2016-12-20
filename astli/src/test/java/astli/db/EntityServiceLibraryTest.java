package astli.db;

import java.sql.SQLException;
import java.util.List;
import net.java.ao.EntityManager;
import net.java.ao.schema.CamelCaseFieldNameConverter;
import net.java.ao.test.converters.NameConverters;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.Hsql;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(EntityServiceLibraryTest.MyDatabaseUpdater.class)
@NameConverters(field = CamelCaseFieldNameConverter.class)
@Jdbc(Hsql.class)
public class EntityServiceLibraryTest {
    
    private EntityManager em;
    private Library lib1;
    private Library lib2;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private EntityService service;
    
    @Before 
    public void setUp() throws SQLException {
        lib1 = em.create(Library.class);
        lib1.setName("group:artifact:1.0");
        lib1.save();
        
        lib2 = em.create(Library.class);
        lib2.setName("group:artifact:2.0");
        lib2.save();
        
        service = new EntityService(em);
    }
    
    @Test
    public void testDontFindNonExistingLibrary() throws Exception {
        Library result = service.findLibraryByMvnIdentifier("groupX:artifact:1.0");
        
        assert(result == null);
    }
    
    @Test
    public void testFindExistingLibrary() throws Exception {
        Library result = service.findLibraryByMvnIdentifier("group:artifact:1.0");

        assert(lib1.equals(result));
    }
    
    @Test
    public void testThrowExceptionWhenMultipleLibrariesWithSameIdentifierFound() throws Exception {
        Library anotherLib2 = em.create(Library.class);
        anotherLib2.setName("group:artifact:2.0");
        anotherLib2.save();

        exception.expect(SQLException.class);
        service.findLibraryByMvnIdentifier("group:artifact:2.0");
    }
    
    @Test
    public void testStoreLibrary() throws Exception {
        String mvnIdentifier = "group:new-artifact:2.0"; 
        Library newLibrary = service.saveLibrary(mvnIdentifier);
        
        assert(newLibrary.getName().equals(mvnIdentifier));
    }

    @Test
    public void testStoreLibraryThatAlreadyExists() throws Exception {
        
        String mvnIdentifier = "group:artifact:1.0"; 
        
        service.saveLibrary(mvnIdentifier);
        
        assert(em.find(Library.class, "NAME = ?", mvnIdentifier).length == 1);
        
    }
    
    @Test 
    public void findLibaries() throws Exception {
        List<Library> foundLibs = service.findLibraries();
        
        assert(foundLibs.contains(lib1));
        assert(foundLibs.contains(lib2));
    }
    
    public static final class MyDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        @SuppressWarnings("unchecked")
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(Library.class);
        }
    }
}
