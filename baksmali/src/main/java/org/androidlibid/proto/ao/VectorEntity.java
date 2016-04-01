package org.androidlibid.proto.ao;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface VectorEntity extends Entity {

    byte[] getVector();
    void setVector(byte[] vector);
    
    @StringLength(StringLength.UNLIMITED)
    String getName();
    void setName(String name);

}
