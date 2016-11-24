package astli.match;

import java.sql.SQLException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import astli.pojo.Fingerprint;
import astli.db.EntityServiceFactory;
import astli.db.FingerprintService;
import astli.db.Package;
import astli.pojo.NodeType;
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
