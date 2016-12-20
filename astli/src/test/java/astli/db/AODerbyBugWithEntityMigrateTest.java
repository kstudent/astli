package astli.db;

import net.java.ao.EntityManager;
import net.java.ao.schema.CamelCaseFieldNameConverter;
import net.java.ao.schema.FieldNameConverter;
import net.java.ao.test.converters.NameConverters;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.Hsql;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Proves that combination of active objects and apache derby is buggy. 
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(AODerbyBugWithEntityMigrateTest.MyDatabaseUpdater.class)
@Jdbc(Hsql.class)
//@Jdbc(DerbyEmbedded.class)
@NameConverters(field = CamelCaseFieldNameConverter.class)
public class AODerbyBugWithEntityMigrateTest {
    
    private EntityManager entityManager;
    
    @Test
    public void doNothing() throws Exception {
    }
    
    public static final class MyDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        @SuppressWarnings("unchecked")
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(Clazz.class);
            entityManager.migrate(Library.class);
        }
    }
    
    java.lang.Class<? extends FieldNameConverter> field() {
        return CamelCaseFieldNameConverter.class;
    }
    
}
