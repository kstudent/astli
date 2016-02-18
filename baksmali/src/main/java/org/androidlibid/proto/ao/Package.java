/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Package extends Entity {

    String getPackageName();
    void setPackageName(String name);

    byte[] getVector();
    void setVector(byte[] vector);
    
    Library getParentLibrary();
    void setParentLibrary(Library parent);
    
    @OneToMany
    public Class[] getClasses();
}
