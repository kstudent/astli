/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import net.java.ao.EntityManager;
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
@NameConverters
@Jdbc(Hsql.class)
public class EntityServiceClassTest {
    
    private EntityManager em;
    
    @Test 
    public void testSaveFingerprint() throws Exception {
        assert(em.count(ClassEntity.class) == 0);
        
        EntityService service = new EntityService(em);
        Vector vector = new BasicVector(5);
        service.saveFingerprint(vector.toBinary(), "vector 1", "", "");
        assert(em.count(ClassEntity.class) == 1);
        
        service.saveFingerprint(vector.toBinary(), "vector 2", "", "");
        assert(em.count(ClassEntity.class) == 2);
        
        service.saveFingerprint(vector.toBinary(), "vector 3", "", "");
        assert(em.count(ClassEntity.class) == 3);
    }
    
    @Test
    public void testIterateOverFingerprints() throws Exception {
        EntityService service = new EntityService(em);

        Vector vector = new BasicVector(5);
        service.saveFingerprint(vector.toBinary(), "vector 1", "", "");
        service.saveFingerprint(vector.toBinary(), "vector 2", "", "");
        service.saveFingerprint(vector.toBinary(), "vector 3", "", "");
        
        int counter = 0;
        for (ClassEntity entity : service.getFingerprintEntities()) {
            counter ++;
        }
        
        assert(counter == 3);
    
    }
    
    @Test
    public void testCountFingerprints() throws Exception {
        EntityService service = new EntityService(em);
        assert(service.countFingerprints() == 0);
        
        Vector vector = new BasicVector(5);
        service.saveFingerprint(vector.toBinary(), "vector 1", "", "");
        service.saveFingerprint(vector.toBinary(), "vector 2", "", "");
        service.saveFingerprint(vector.toBinary(), "vector 3", "", "");
        
        assert(service.countFingerprints() == 3);
    }
    
    @Test
    public void testDeleteAllFingerprints() throws Exception {
        EntityService service = new EntityService(em);
        
        assert(em.count(ClassEntity.class) == 0);
        
        Vector vector = new BasicVector(5);
        service.saveFingerprint(vector.toBinary(), "vector 1", "", "");
        service.saveFingerprint(vector.toBinary(), "vector 2", "", "");
        service.saveFingerprint(vector.toBinary(), "vector 3", "", "");
        
        assert(em.count(ClassEntity.class) == 3);
        
        service.deleteAllFingerprints();
        
        assert(em.count(ClassEntity.class) == 0);
    }
    
    public static final class FingerprintServiceTestDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(ClassEntity.class, PackageEntity.class, LibraryEntity.class);
        }
    }
}
