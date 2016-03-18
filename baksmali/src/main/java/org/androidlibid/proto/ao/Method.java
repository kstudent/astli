package org.androidlibid.proto.ao;

import net.java.ao.schema.Indexed;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Method extends VectorEntity {
   
    public Clazz getClazz();
    public void setClazz(Clazz clazz);
    
    @Indexed
    public void setLength(double length);
    public double getLength(); 
    
}
