/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.ao.EntityManager;
import net.java.ao.builder.EntityManagerBuilder;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassEntityServiceFactory {

    private static EntityManager entityManager = null;
    
    //so. here the em instance is shared accorss service instances, 
    //which can be troublesome for concurrency. (reason for sharing: tried to
    //solve the SQLException on em.migrate(), but didnt work)
    //@TODO Evaluate if this works with concurrency
    public static ClassEntityService createService() {

        if(entityManager == null) {
            JdbcProperties jdbcProperties = jdbcProperties();

            entityManager = EntityManagerBuilder
                    .url(jdbcProperties.url)
                    .username(jdbcProperties.username)
                    .password(jdbcProperties.passord)
                    .auto().build();

            try {
                entityManager.migrate(ClassEntity.class);
            } catch (Exception e) {
                //ou nou...
                Logger.getLogger(ClassEntityServiceFactory.class.getName()).log(Level.SEVERE, "entityManager.migrate... again...");
            }
        }

        return new ClassEntityService(entityManager);
    }
    
    private static JdbcProperties jdbcProperties() {
        final InputStream is = ClassEntityService.class.getResourceAsStream("/db.properties");
        if (is == null) {
            throw new RuntimeException("Unable to locate db.properties");
        }

        try {
            final Properties props = new Properties();
            props.load(is);
            return new JdbcProperties(
                    props.getProperty("db.uri"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load db.properties");
        } finally {

            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
