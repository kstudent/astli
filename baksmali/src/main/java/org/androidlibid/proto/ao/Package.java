package org.androidlibid.proto.ao;

import net.java.ao.OneToMany;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Package extends VectorEntity {

    Library getLibrary();
    void setLibrary(Library library);
    
    @OneToMany
    public Clazz[] getClazzes();
    }
