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
    public void testFindMethodsByLength() throws SQLException {
        
        Method m1 = createMethod("method1", 0,  1,  2,  3,  4);
        Method m2 = createMethod("method2", 0, 10, 20, 30, 40);
        Method m3 = createMethod("method3", 0,  1,  2,  3,  4);
        
        List<Method> methods = service.findMethodsByLength(m1.getLength(), 1);
        
        assert( methods.size() == 2);
        assert( methods.contains(m1));
        assert(!methods.contains(m2));
        assert( methods.contains(m3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDontFindMethodsWithZeroSize() throws SQLException {
        service.findMethodsByLength(1, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDontFindMethodsWithZeroLength() throws SQLException {
        service.findMethodsByLength(0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDontFindMethodsWithNegativeLength() throws SQLException {
        service.findMethodsByLength(-1, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDontFindMethodsWithNegativeSize() throws SQLException {
        service.findMethodsByLength(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDontFindMethodsWithinNegativeOrbit() throws SQLException {
        service.findMethodsByLength(1, 2);
    }
    
    @Test
    public void testSaveMethod() throws SQLException {
        
        Clazz clazzEntity = em.create(Clazz.class);
        clazzEntity.setName("ClassX");
        clazzEntity.save();
        
        Fingerprint methodFingerprint = new Fingerprint(7, 1, 100, 12 , 0);
        String methodName = "<init>():void";
        Method methodEntity = service.saveMethod(
            methodFingerprint.getFeatureVector().toBinary(),
            methodName,
            methodFingerprint.getLength(), 
            clazzEntity
        );
        
        Method[] foundMethods = em.find(Method.class, "ID = ?", methodEntity.getID());
        
        assert(foundMethods.length == 1);
        
        Method foundMethod = foundMethods[0];
        
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

    private Method createMethod(String name, double... values) throws SQLException {
        Method entity = em.create(Method.class);
        Fingerprint method = new Fingerprint(values);
        entity.setVector(method.getFeatureVector().toBinary());
        entity.setLength(method.getLength());
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
            entityManager.migrate(Method.class, Clazz.class, Package.class, Library.class);
        }
    }
}