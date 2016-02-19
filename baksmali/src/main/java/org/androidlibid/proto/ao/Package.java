package org.androidlibid.proto.ao;

import net.java.ao.Mutator;
import net.java.ao.OneToMany;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Package extends VectorEntity {

    @Mutator("LIBRARY_ID")
    Library getLibrary();
    @Mutator("LIBRARY_ID")
    void setLibrary(Library library);
    
    @OneToMany
    public Class[] getClasses();
}
