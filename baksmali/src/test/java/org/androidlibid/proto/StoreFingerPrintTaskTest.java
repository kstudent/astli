/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import org.androidlibid.proto.ao.EntityService;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreFingerPrintTaskTest {
    
    @Test
    public void testExtractPackageName() throws Exception {
        
        ClassDef classDef          = Mockito.mock(ClassDef.class);
        baksmaliOptions options    = Mockito.mock(baksmaliOptions.class);
        EntityService service = Mockito.mock(EntityService.class);
        
        StoreFingerprintTask task = new StoreFingerprintTask(classDef, options, service);
        String className = "Ltld/domain/subdomain/project/package/ClassName;";
                
        String packageName = task.extractPackageName(className);
        assert(packageName.equals("tld/domain/subdomain/project/package"));
    }
    
}
