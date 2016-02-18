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
@Data(EntityServicePackageTest.MyDatabaseUpdater.class)
@NameConverters
@Jdbc(Hsql.class)
public class EntityServicePackageTest {
    
    private EntityManager em;
    private Library lib1;
    private Library lib2;
    private Package package1;
    private Package package2;
    private byte[] bytes = {};

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before 
    public void setUp() throws SQLException {
        lib1 = em.create(Library.class);
        lib1.setMvnIdentifier("group:artifact:1.0");
        lib1.setVector(bytes);
        lib1.save();
        
        lib2 = em.create(Library.class);
        lib2.setMvnIdentifier("group:artifact:2.0");
        lib2.setVector(bytes);
        lib2.save();
        
        package1 = em.create(Package.class);
        package1.setPackageName("package1");
        package1.setParentLibrary(lib1);
        package1.setVector(bytes);
        package1.save();
        
        package2 = em.create(Package.class);
        package2.setPackageName("package2");
        package2.setParentLibrary(lib1);
        package2.setVector(bytes);
        package2.save();
    }
    
    @Test
    public void testDontFindNonExistingPackage() throws Exception {
        EntityService service = new EntityService(em);
        
        Package result = service.findPackageByNameAndLib("packageX", lib1);
        assert(result == null);
    }
    
    @Test
    public void testDontFindPackageInWrongLib() throws Exception {
        EntityService service = new EntityService(em);
        
        Package result = service.findPackageByNameAndLib("package1", lib2);
        assert(result == null);
    }
    
    @Test
    public void testFindExistingPackage() throws Exception {
        EntityService service = new EntityService(em);
        
        Package result = service.findPackageByNameAndLib("package1", lib1);
        assert(package1.equals(result));
    }
    
    @Test
    public void testThrowExceptionWhenMultiplePackagesWithSameIdentifierFound() throws Exception {
        EntityService service = new EntityService(em);
         
        Package anotherPackage1 = em.create(Package.class);
        anotherPackage1.setPackageName("package1");
        anotherPackage1.setParentLibrary(lib1);
        anotherPackage1.setVector(bytes);
        anotherPackage1.save();

        exception.expect(SQLException.class);
        service.findPackageByNameAndLib("package1", lib1);
    }
    
    @Test
    public void testSavePackage() throws Exception {
        EntityService service = new EntityService(em);
        
        String packageName = "package3";
        Package pckg = service.savePackage(packageName, lib1);
        
        assert(pckg != null);
        assert(packageName.equals(pckg.getPackageName()));
        assert(em.find(Package.class, 
                "PACKAGE_NAME = ? AND PARENT_LIBRARY_ID = ?", 
                packageName, lib1.getID()).length == 1);
        
    }

    @Test
    public void testSavePackageThatAlreadyExists() throws Exception {
        EntityService service = new EntityService(em);
        
        String packageName = "package1";
        Package pckg = service.savePackage(packageName, lib1);
        
        assert(pckg != null);
        assert(packageName.equals(pckg.getPackageName()));
        assert(em.find(Package.class, 
                "PACKAGE_NAME = ? AND PARENT_LIBRARY_ID = ?", 
                packageName, lib1.getID()).length == 1);
    }
    
    public static final class MyDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(Package.class);
        }
    }
}
