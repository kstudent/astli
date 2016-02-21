package org.androidlibid.proto.ao;

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
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(EntityMigrateTest.MyDatabaseUpdater.class)
@Jdbc(Hsql.class)  
@NameConverters(field = CamelCaseFieldNameConverter.class)
public class EntityMigrateTest {
    
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
            entityManager.migrate(Class.class);
            entityManager.migrate(Library.class);
        }
    }
    
    java.lang.Class<? extends FieldNameConverter> field() {
        return CamelCaseFieldNameConverter.class;
    }
    
}
