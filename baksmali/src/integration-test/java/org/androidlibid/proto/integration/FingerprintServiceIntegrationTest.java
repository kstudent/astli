/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.integration;

import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.ClassEntity;
import org.androidlibid.proto.ao.ClassEntityService;
import org.androidlibid.proto.ao.ClassEntityServiceFactory;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintServiceIntegrationTest {
    
    private ClassEntityService service; 
    
    @Test
    public void testRetrieveAllFingerprints() {
        
        service = ClassEntityServiceFactory.createService();
        
        int counter = 0;
        
        for(ClassEntity entity : service.getFingerprintEntities()) {
            Fingerprint print = new Fingerprint(entity);
            counter++;
            System.out.println(print);
        }
        
        System.out.println("amount of fingerprints: " + counter);
    }
    
    
}
