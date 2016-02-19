package org.androidlibid.proto.ao;

import net.java.ao.Entity;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface VectorEntity extends Entity {

    byte[] getVector();
    void setVector(byte[] vector);
    
    String getName();
    void setName(String name);

}
