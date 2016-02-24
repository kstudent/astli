package org.androidlibid.proto.ao;

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
import org.la4j.vector.dense.BasicVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(EntityServicePackageTest.MyDatabaseUpdater.class)
@NameConverters(field = CamelCaseFieldNameConverter.class)
@Jdbc(Hsql.class)
public class EntityServicePackageTest {
    
    private EntityManager em;
    private Library lib1;
    private Library lib2;
    private Package package1;
    private Package package2;
    private byte[] zeroBytes;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private EntityService service;
    
    @Before 
    public void setUp() throws SQLException {
        zeroBytes = new BasicVector(1).toBinary();
        
        lib1 = em.create(Library.class);
        lib1.setName("group:artifact:1.0");
        lib1.setVector(zeroBytes);
        lib1.save();
        
        lib2 = em.create(Library.class);
        lib2.setName("group:artifact:2.0");
        lib2.setVector(zeroBytes);
        lib2.save();
        
        package1 = em.create(Package.class);
        package1.setName("package1");
        package1.setLibrary(lib1);
        package1.setVector(zeroBytes);
        package1.save();
        
        package2 = em.create(Package.class);
        package2.setName("package2");
        package2.setLibrary(lib1);
        package2.setVector(zeroBytes);
        package2.save();
        
        service = new EntityService(em, zeroBytes);
    }
    
    @Test
    public void testDontFindNonExistingPackage() throws Exception {
        
        Package result = service.findPackageByNameAndLib("packageX", lib1);
        assert(result == null);
    }
    
    @Test
    public void testDontFindPackageInWrongLib() throws Exception {
        Package result = service.findPackageByNameAndLib("package1", lib2);
        assert(result == null);
    }
    
    @Test
    public void testFindExistingPackage() throws Exception {
        Package result = service.findPackageByNameAndLib("package1", lib1);
        assert(package1.equals(result));
    }
    
    @Test
    public void testThrowExceptionWhenMultiplePackagesWithSameIdentifierFound() throws Exception {
        Package anotherPackage1 = em.create(Package.class);
        anotherPackage1.setName("package1");
        anotherPackage1.setLibrary(lib1);
        anotherPackage1.setVector(zeroBytes);
        anotherPackage1.save();

        exception.expect(SQLException.class);
        service.findPackageByNameAndLib("package1", lib1);
    }
    
    @Test
    public void testSavePackage() throws Exception {
        String packageName = "package3";
        Package pckg = service.savePackage(packageName, lib1);
        
        assert(pckg != null);
        assert(packageName.equals(pckg.getName()));
        assert(em.find(Package.class, 
                "NAME = ? AND LIBRARYID = ?", 
                packageName, lib1.getID()).length == 1);
        
    }

    @Test
    public void testSavePackageThatAlreadyExists() throws Exception {
        String packageName = "package1";
        Package pckg = service.savePackage(packageName, lib1);
        
        assert(pckg != null);
        assert(packageName.equals(pckg.getName()));
        assert(em.find(Package.class, 
                "NAME = ? AND LIBRARYID = ?", 
                packageName, lib1.getID()).length == 1);
    }
    
    @Test
    public void getLibrariesWithLevel() throws Exception {
        
        Package pckg1 = em.create(Package.class);
        pckg1.setName("org.package.test");
        pckg1.setLibrary(lib1);
        pckg1.setVector(zeroBytes);
        pckg1.save();
        
        Package pckg2 = em.create(Package.class);
        pckg2.setName("org.package.test.subpackage1");
        pckg2.setLibrary(lib1);
        pckg2.setVector(zeroBytes);
        pckg2.save();
        
        Package pckg3 = em.create(Package.class);
        pckg3.setName("org.package.test.subpackage2");
        pckg3.setLibrary(lib1);
        pckg3.setVector(zeroBytes);
        pckg3.save();
        
        List<Package> pckgs;
        
        pckgs = service.getPackagesWithLevel(0);
        assert(pckgs.size() == 2);
        
        pckgs = service.getPackagesWithLevel(1);
        assert(pckgs.isEmpty());
        
        pckgs = service.getPackagesWithLevel(2);
        assert(pckgs.size() == 1);
        assert(pckgs.get(0).equals(pckg1));
        
        pckgs = service.getPackagesWithLevel(3);
        assert(pckgs.size() == 2);
        
    }
    
    
    public static final class MyDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        @SuppressWarnings("unchecked")
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(Library.class, Package.class, Class.class);
        }
    }
}
