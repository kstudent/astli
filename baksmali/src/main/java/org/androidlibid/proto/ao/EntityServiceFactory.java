/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import net.java.ao.EntityManager;
import net.java.ao.builder.EntityManagerBuilder;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class EntityServiceFactory {
    
    public static EntityService createService() throws SQLException {

        JdbcProperties jdbcProperties = jdbcProperties();

        EntityManager entityManager = EntityManagerBuilder
                .url(jdbcProperties.url)
                .username(jdbcProperties.username)
                .password(jdbcProperties.passord)
                .auto().build();

        entityManager.migrate(Class.class);

        return new EntityService(entityManager);
    }
    
    private static JdbcProperties jdbcProperties() {
        final InputStream is = EntityService.class.getResourceAsStream("/db.properties");
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
