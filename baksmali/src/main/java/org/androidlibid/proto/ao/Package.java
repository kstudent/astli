package org.androidlibid.proto.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.StringLength;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Package extends Entity {

    Library getLibrary();
    void setLibrary(Library library);
    
    @StringLength(StringLength.UNLIMITED)
    public void setName(String name);
    public String getName();
    
    @OneToMany
    public Clazz[] getClazzes();
    }
