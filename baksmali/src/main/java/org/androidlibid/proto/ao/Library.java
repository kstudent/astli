package org.androidlibid.proto.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Library extends VectorEntity {
    
    @OneToMany
    public Package[] getPackages();
}
