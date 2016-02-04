/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.java.ao.EntityManager;
import net.java.ao.RawEntity;
import net.java.ao.test.converters.NameConverters;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.DerbyEmbedded;
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
@Data(FingerprintServiceTest.FingerprintServiceTestDatabaseUpdater.class)
@NameConverters
@Jdbc(DerbyEmbedded.class)
public class FingerprintServiceTest {
    
    private EntityManager em;
    
    @Test 
    public void testSaveFingerprint() throws Exception {
        assert(em.count(FingerprintEntity.class) == 0);
        
        FingerprintService service = new FingerprintService(em);
        Vector vector = new BasicVector(5);
        service.saveFingerprint(vector, "vector 1");
        assert(em.count(FingerprintEntity.class) == 1);
        
        service.saveFingerprint(vector, "vector 2");
        assert(em.count(FingerprintEntity.class) == 2);
        
        service.saveFingerprint(vector, "vector 3");
        assert(em.count(FingerprintEntity.class) == 3);
    }
    
    @Test
    public void testIterateOverFingerprints() throws Exception {
        FingerprintService service = new FingerprintService(em);

        Vector vector = new BasicVector(5);
        service.saveFingerprint(vector, "vector 1");
        service.saveFingerprint(vector, "vector 2");
        service.saveFingerprint(vector, "vector 3");
        
        int counter = 0;
        for (FingerprintEntity entity : service.getFingerprints()) {
            System.out.println(entity.getName());
            counter ++;
        }
        
        assert(counter == 3);
    
    }
    
    public static final class FingerprintServiceTestDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(FingerprintEntity.class);
        }
    }
}
