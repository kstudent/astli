package org.androidlibid.proto.integration;

import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.Class;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintServiceIntegrationTest {
    
    private EntityService service; 
    
    @Test
    public void testRetrieveAllFingerprints() throws Exception {
        
        service = EntityServiceFactory.createService();
        
        int counter = 0;
        
        for(Class entity : service.getClasses()) {
            Fingerprint print = new Fingerprint(entity);
            counter++;
            System.out.println(print);
        }
        
        System.out.println("amount of fingerprints: " + counter);
    }
    
    
}
