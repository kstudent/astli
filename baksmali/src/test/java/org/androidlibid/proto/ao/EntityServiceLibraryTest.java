package org.androidlibid.proto.ao;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.SQLException;
import net.java.ao.EntityManager;
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
@NameConverters
@Jdbc(Hsql.class)
public class EntityServiceLibraryTest {
    
    private EntityManager em;
    private LibraryEntity lib1;
    private LibraryEntity lib2;
    private byte[] bytes = {};

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before 
    public void setUp() throws SQLException {
        lib1 = em.create(LibraryEntity.class);
        lib1.setMvnIdentifier("group:artifact:1.0");
        lib1.setVector(bytes);
        lib1.save();
        
        lib2 = em.create(LibraryEntity.class);
        lib2.setMvnIdentifier("group:artifact:2.0");
        lib2.setVector(bytes);
        lib2.save();
    }
    
    @Test
    public void testDontFindNonExistingLibrary() throws Exception {
        EntityService service = new EntityService(em);
        
        LibraryEntity result = service.findLibraryByMvnIdentifier("groupX:artifact:1.0");
        
        assert(result == null);
    }
    
    @Test
    public void testFindExistingLibrary() throws Exception {
        EntityService service = new EntityService(em);
        
        LibraryEntity result = service.findLibraryByMvnIdentifier("group:artifact:1.0");

        assert(lib1.equals(result));
    }
    
    @Test
    public void testThrowExceptionWhenMultipleLibrariesWithSameIdentifierFound() throws Exception {
        EntityService service = new EntityService(em);
         
        LibraryEntity anotherLib2 = em.create(LibraryEntity.class);
        anotherLib2.setMvnIdentifier("group:artifact:2.0");
        anotherLib2.setVector(bytes);
        anotherLib2.save();

        exception.expect(SQLException.class);
        service.findLibraryByMvnIdentifier("group:artifact:2.0");
    }
    
    @Test
    public void testStoreLibrary() throws Exception {
        EntityService service = new EntityService(em);
        
        String mvnIdentifier = "group:new-artifact:2.0"; 
        LibraryEntity newLibrary = service.saveLibrary(mvnIdentifier);
        
        assert(newLibrary.getMvnIdentifier().equals(mvnIdentifier));
    }

    @Test
    public void testStoreLibraryThatAlreadyExists() throws Exception {
        EntityService service = new EntityService(em);
        
        String mvnIdentifier = "group:artifact:1.0"; 
        
        service.saveLibrary(mvnIdentifier);
        
        assert(em.find(LibraryEntity.class, "MVN_IDENTIFIER = ?", mvnIdentifier).length == 1);
        
    }
    
    public static final class MyDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(LibraryEntity.class);
        }
    }
}
