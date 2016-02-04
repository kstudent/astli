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

//@Implementation(Fingerprint.class)
public interface FingerprintEntity extends Entity {

    String getName();

    byte[] getVector();

    void setName(String name);

    void setVector(byte[] vector);
    
}
