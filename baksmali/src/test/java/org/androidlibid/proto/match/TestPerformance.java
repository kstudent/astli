package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.ao.Package;
import org.androidlibid.proto.ast.NodeType;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class TestPerformance {

    @Test
    public void testPerformance() throws SQLException {
       
        FingerprintService s = new FingerprintService(EntityServiceFactory.createService());
        
        IntStream.range(0, 100)
            .boxed()
            .flatMap(index -> {
                Fingerprint fp = new Fingerprint();
                fp.incrementFeatureBy(index.shortValue(), NodeType.DRC);
                fp.setSignature(":");
                return s.findPackagesWithSameMethods(fp);
            })
//            .map(pckg -> s.createHierarchyFromPackage(pckg))
            .forEach(pckg -> {});
            
        System.out.println("Done.");
                
        
    }
    
}
