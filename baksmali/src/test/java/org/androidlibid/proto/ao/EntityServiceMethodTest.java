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
import org.androidlibid.proto.Fingerprint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.la4j.vector.dense.BasicVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(EntityServiceMethodTest.FingerprintServiceTestDatabaseUpdater.class)
@NameConverters(field = CamelCaseFieldNameConverter.class)
@Jdbc(Hsql.class)
public class EntityServiceMethodTest {
    
    private EntityManager em;
    private byte[] zeroBytes;
    private EntityService service;
    
    @Before
    public void setUp() throws SQLException {
        zeroBytes = new BasicVector(5).toBinary();
        service = new EntityService(em, zeroBytes);
    }
        
    @Test
    public void testSaveMethod() throws SQLException {
        
        Clazz clazzEntity = em.create(Clazz.class);
        clazzEntity.setName("ClassX");
        clazzEntity.save();
        
        Fingerprint methodFingerprint = new Fingerprint(7, 1, 100, 12 , 0);
        String methodName = "<init>():void";
        String signature = "V:V";
        
        FingerprintEntity methodEntity = service.saveMethod(
            methodFingerprint.getBinaryFeatureVector(),
            methodName,
            signature,
            clazzEntity
        );
        
        FingerprintEntity[] foundMethods = em.find(FingerprintEntity.class, "ID = ?", methodEntity.getID());
        
        assert(foundMethods.length == 1);
        
        FingerprintEntity foundMethod = foundMethods[0];
        
        assert(foundMethod.equals(methodEntity));
        assert(foundMethod.getClazz().equals(clazzEntity));
    }
    
    @Test 
    public void testCountMethods() throws SQLException {
        
        assert(service.countMethods() == 0);
        
        createMethod("1", 0, 1, 2, 3, 4);
        createMethod("2", 0, 1, 2, 3, 4);
        createMethod("3", 0, 1, 2, 3, 4);
        
        assert(service.countMethods() == 3);
    }

    private FingerprintEntity createMethod(String name, double... values) throws SQLException {
        FingerprintEntity entity = em.create(FingerprintEntity.class);
        Fingerprint method = new Fingerprint(values);
        entity.setVector(method.getBinaryFeatureVector());
        entity.setSignature(method.getSignature());
        entity.setName(name);
        entity.save(); 
        return entity;
    }
        
    public static final class FingerprintServiceTestDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        @SuppressWarnings("unchecked")
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(FingerprintEntity.class, Clazz.class, Package.class, Library.class);
        }
    }
}
