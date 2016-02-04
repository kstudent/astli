/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
final class JdbcProperties {
    
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
