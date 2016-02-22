package org.androidlibid.proto.ao;

import net.java.ao.EntityManager;
import net.java.ao.schema.CamelCaseFieldNameConverter;
import net.java.ao.test.converters.NameConverters;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.Hsql;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(EntityServiceClassTest.FingerprintServiceTestDatabaseUpdater.class)
@NameConverters(field = CamelCaseFieldNameConverter.class)
@Jdbc(Hsql.class)
public class EntityServiceClassTest {
    
    private EntityManager em;
    
    @Test 
    public void testSaveFingerprintCount() throws Exception {
        assert(em.count(Class.class) == 0);
        Vector vector = new BasicVector(5);
        
        EntityService service = new EntityService(em, vector.toBinary());
        service.saveClass(vector.toBinary(), "vector 1", "", "");
        assert(em.count(Class.class) == 1);
        
        service.saveClass(vector.toBinary(), "vector 2", "", "");
        assert(em.count(Class.class) == 2);
        
        service.saveClass(vector.toBinary(), "vector 3", "", "");
        assert(em.count(Class.class) == 3);
    }
    
    @Test 
    public void testSaveFingerprint() throws Exception {
        
        String packageName   = "Lorg.lib.package";
        String mvnIdentifier = "libGroupId:libArtifactId:0.1";
        
        assert(em.count(Class.class)   == 0);
        assert(em.count(Library.class) == 0);
        assert(em.count(Package.class) == 0);
        
        Vector vector = new BasicVector(5);
        EntityService service = new EntityService(em, vector.toBinary());
        
        service.saveClass(vector.toBinary(), "vector 1", packageName, mvnIdentifier);
        service.saveClass(vector.toBinary(), "vector 2", packageName, mvnIdentifier);
        service.saveClass(vector.toBinary(), "vector 3", packageName, mvnIdentifier);
        
        Class[] classes = em.find(Class.class, "NAME = ?", "vector 1");
        assert(classes.length == 1);
        assert(classes[0].getName() != null);
        assert(classes[0].getName().equals("vector 1"));
        assert(classes[0].getVector() != null);
        
        Package[] packages = em.find(Package.class, "NAME = ?", packageName);
        assert(packages.length == 1);
        assert(packages[0].getName() != null);
        assert(packages[0].getName().equals(packageName));
        assert(packages[0].getVector() != null);
        assert(classes[0].getPackage().equals(packages[0]));
        
        Library[] libs = em.find(Library.class, "NAME = ?", mvnIdentifier);
        assert(libs.length == 1);
        assert(libs[0].getName() != null);
        assert(libs[0].getName().equals(mvnIdentifier));
        assert(libs[0].getVector() != null);
        assert(packages[0].getLibrary().equals(libs[0]));
        
        assert(em.count(Class.class)   == 3);
        assert(em.count(Library.class) == 1);
        assert(em.count(Package.class) == 1);
        
    }
    
    @Test 
    public void testSaveFingerprintsWithDifferentLibs() throws Exception {
        
        String packageName   = "Lorg.lib.package";
        String lib1Identifier = "libGroupId:libArtifactId1:0.1";
        String lib2Identifier = "libGroupId:libArtifactId2:0.1";
        String className = "class";
        
        assert(em.count(Class.class)   == 0);
        assert(em.count(Library.class) == 0);
        assert(em.count(Package.class) == 0);
        
        Vector vector = new BasicVector(5);
        EntityService service = new EntityService(em, vector.toBinary());
        
        service.saveClass(vector.toBinary(), className, packageName, lib1Identifier);
        service.saveClass(vector.toBinary(), className, packageName, lib2Identifier);
        
        Class[] classes = em.find(Class.class, "NAME = ?", className);
        assert(classes.length == 2);
        assert(classes[0].getName() != null);
        assert(classes[0].getName().equals(className));
        assert(classes[0].getVector() != null);
        
        Package[] packages = em.find(Package.class, "NAME = ?", packageName);
        assert(packages.length == 2);
        assert(packages[0].getName() != null);
        assert(packages[0].getName().equals(packageName));
        assert(packages[0].getVector() != null);
        
        Library[] libs = em.find(Library.class, "NAME = ?", lib1Identifier);
        assert(libs.length == 1);
        
        libs = em.find(Library.class, "NAME = ?", lib2Identifier);
        assert(libs.length == 1);
        
        assert(em.count(Class.class)   == 2);
        assert(em.count(Library.class) == 2);
        assert(em.count(Package.class) == 2);
    }
    
    @Test
    public void testIterateOverFingerprints() throws Exception {
        Vector vector = new BasicVector(5);
        
        EntityService service = new EntityService(em, vector.toBinary());

        service.saveClass(vector.toBinary(), "vector 1", "", "");
        service.saveClass(vector.toBinary(), "vector 2", "", "");
        service.saveClass(vector.toBinary(), "vector 3", "", "");
        
        int counter = 0;
        for (Class entity : service.getClasses()) {
            counter ++;
        }
        
        assert(counter == 3);
    
    }
    
    @Test
    public void testCountFingerprints() throws Exception {
        Vector vector = new BasicVector(5);
        
        EntityService service = new EntityService(em, vector.toBinary());
        assert(service.countClasses() == 0);
        
        service.saveClass(vector.toBinary(), "vector 1", "", "");
        service.saveClass(vector.toBinary(), "vector 2", "", "");
        service.saveClass(vector.toBinary(), "vector 3", "", "");
        
        assert(service.countClasses() == 3);
    }
    
    @Test
    public void testDeleteAllFingerprints() throws Exception {
        
        assert(em.count(Class.class) == 0);
        Vector vector = new BasicVector(5);
        
        EntityService service = new EntityService(em, vector.toBinary());
        
        service.saveClass(vector.toBinary(), "vector 1", "", "");
        service.saveClass(vector.toBinary(), "vector 2", "", "");
        service.saveClass(vector.toBinary(), "vector 3", "", "");
        
        assert(em.count(Class.class) == 3);
        
        service.truncateTables();
        
        assert(em.count(Class.class) == 0);
    }
    
    public static final class FingerprintServiceTestDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        @SuppressWarnings("unchecked")
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(Class.class, Package.class, Library.class);
        }
    }
}
