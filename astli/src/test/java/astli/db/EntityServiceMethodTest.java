package astli.db;

import java.sql.SQLException;
import net.java.ao.EntityManager;
import net.java.ao.schema.CamelCaseFieldNameConverter;
import net.java.ao.test.converters.NameConverters;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.Hsql;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import astli.pojo.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(EntityServiceMethodTest.MyDbUpdater.class)
@NameConverters(field = CamelCaseFieldNameConverter.class)
@Jdbc(Hsql.class)
public class EntityServiceMethodTest {
    
    private EntityManager em;
    private EntityService service;
    
    @Before
    public void setUp() throws SQLException {
        service = new EntityService(em);
    }
        
    @Test
    public void testSaveMethod() throws SQLException {
        
        Clazz clazzEntity = em.create(Clazz.class);
        clazzEntity.setName("ClassX");
        clazzEntity.save();
        
        String methodName = "<init>():void";
        String signature = "V:V";
        
        Method methodEntity = service.saveMethod(
            ArrayUtils.short2LEByte(new short[]{7, 1, 100, 12 , 0}),
            methodName,
            signature,
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

    private Method createMethod(String name, int... values) throws SQLException {
        Method entity = em.create(Method.class);
        entity.setVector(ArrayUtils.truncateIntToLEByteArray(values));
        entity.setSignature("");
        entity.setName(name);
        entity.save(); 
        return entity;
    }
        
    public static final class MyDbUpdater implements DatabaseUpdater
    {
        @Override
        @SuppressWarnings("unchecked")
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(Method.class, Clazz.class, Package.class, Library.class);
        }
    }
}
