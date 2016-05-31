package org.androidlibid.proto.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.StringLength;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Library extends Entity {
    
    @OneToMany
    public Package[] getPackages();
    
    @StringLength(StringLength.UNLIMITED)
    public void setName(String name);
    public String getName();

}
