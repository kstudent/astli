package astli.db;

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
    
    @SuppressWarnings("unchecked")
    public static EntityService createService() throws SQLException {

        JdbcProperties jdbcProperties = jdbcProperties();
        
//        connectToDBAndCreateIndex(jdbcProperties);
        
        EntityManager entityManager = EntityManagerBuilder
                .url(jdbcProperties.url)
                .username(jdbcProperties.username)
                .password(jdbcProperties.password)
                .auto().build();

        entityManager.migrate(MethodE.class, Clazz.class, Package.class, Library.class);
       
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

    private static void connectToDBAndCreateIndex(JdbcProperties jdbcProperties) throws SQLException {
        
//        Connection connection = DriverManager.getConnection(jdbcProperties.url, jdbcProperties.username, jdbcProperties.password);
        
    }
}
