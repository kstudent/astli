package astli.db;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

@Preload
public interface FingerprintEntity extends Entity {
   
    public Clazz getClazz();
    public void setClazz(Clazz clazz);
    
    byte[] getVector();
    void setVector(byte[] vector);
    
    @StringLength(StringLength.UNLIMITED)
    String getName();
    void setName(String name);

    @StringLength(StringLength.UNLIMITED)
    public void setSignature(String signature);
    public String getSignature();
    
}
