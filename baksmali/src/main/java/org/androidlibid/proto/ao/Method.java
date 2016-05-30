package org.androidlibid.proto.ao;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */

public interface Method extends VectorEntity {
   
    public Clazz getClazz();
    public void setClazz(Clazz clazz);
    
    public void setLength(double length);
    public double getLength(); 
    
    public void setSignature(String signature);
    public String getSignature();
    
}
