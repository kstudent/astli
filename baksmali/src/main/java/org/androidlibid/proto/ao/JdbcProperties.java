package org.androidlibid.proto.ao;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class JdbcProperties {
    
    public final String url;
    public final String username;
    public final String passord;

    public JdbcProperties(String url, String username, String passord)
    {
        this.url      = url;
        this.username = username;
        this.passord  = passord;
    }
}
