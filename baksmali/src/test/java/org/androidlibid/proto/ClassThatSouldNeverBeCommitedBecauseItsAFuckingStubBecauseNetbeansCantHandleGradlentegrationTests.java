/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import org.androidlibid.proto.ao.ClassEntityService;
import org.androidlibid.proto.ao.ClassEntityServiceFactory;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassThatSouldNeverBeCommitedBecauseItsAFuckingStubBecauseNetbeansCantHandleGradlentegrationTests {
    void clearDB() throws Exception {
        ClassEntityService service = ClassEntityServiceFactory.createService();
        service.deleteAllFingerprints();
        
    } 
}
