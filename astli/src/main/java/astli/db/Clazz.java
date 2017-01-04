package astli.db;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.StringLength;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Clazz extends Entity{
   
    public void setPackage(Package pckg);
    public Package getPackage();
    
    @StringLength(StringLength.UNLIMITED)
    public void setName(String name);
    public String getName();
    
    @OneToMany
    public MethodE[] getMethods();
    
}
