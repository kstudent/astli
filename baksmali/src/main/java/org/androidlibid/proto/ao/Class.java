/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import net.java.ao.Entity;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Class extends Entity {

    String getClassName();

    byte[] getVector();

    void setClassName(String name);

    void setVector(byte[] vector);
    
    public Package getParentPackage();
    
    public void setPackage(Package parentPackage);
    
}
