package org.androidlibid.proto.ao;

import net.java.ao.Mutator;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Class extends VectorEntity {
   
    @Mutator("PACKAGE_ID")
    public void setPackage(Package pckg);
    @Mutator("PACKAGE_ID")
    public Package getPackage();
    
}
