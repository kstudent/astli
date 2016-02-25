package org.androidlibid.proto.ao;

import net.java.ao.OneToMany;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Clazz extends VectorEntity {
   
    public void setPackage(Package pckg);
    public Package getPackage();
    
    @OneToMany
    public Method[] getMethods();
    
}
