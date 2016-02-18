/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.lang.reflect.Array;
import net.java.ao.EntityManager;
import net.java.ao.RawEntity;
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
@NameConverters
public class EntityMigrateTest {
    
    private EntityManager entityManager;
    
    @Test
    public void doNothing() throws Exception {
    }
    
    public static final class MyDatabaseUpdater implements DatabaseUpdater
    {
        @Override
        public void update(EntityManager entityManager) throws Exception
        {
            entityManager.migrate(ClassEntity.class);
            entityManager.migrate(LibraryEntity.class);
        }
    }
}
