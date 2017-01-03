package astli.find;

import astli.db.EntityService;
import java.util.HashMap;
import java.util.Map;
import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import org.junit.Test;
import java.sql.SQLException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FindByNeedleTest {
    
    @Test
    public void testFilterByParticularity() throws SQLException {
        PackageHierarchy h = new PackageHierarchy("pckg");
        
        Map<String, Fingerprint> classAPrints = new HashMap<>();
        Map<String, Fingerprint> classBPrints = new HashMap<>();
        classAPrints.put("discartedA", mockFingerprint(1, "a"));
        classAPrints.put("chosenB"   , mockFingerprint(10,"b"));
        classBPrints.put("discatedC" , mockFingerprint(2, "c"));
        classBPrints.put("discatedD" , mockFingerprint(3, "d")); 
        classBPrints.put("chosenE"   , mockFingerprint(12,"e"));
        h.addMethods("classA", classAPrints);
        h.addMethods("classB", classBPrints);
        
        EntityService service = mockService();
        
        int minParticularity = 9;
        int maxNeedle  = 3;
        
        FindByNeedle f = new FindByNeedle(service, minParticularity, maxNeedle);
        
        f.findCandidates(h).forEach(pckg -> {});
        
        verifyFingeprintWasNotChosen(service, "a");
        verifyFingeprintWasChosen(service,    "b");
        verifyFingeprintWasNotChosen(service, "c");
        verifyFingeprintWasNotChosen(service, "d"); //discarted because particularity lower min particularity!
        verifyFingeprintWasChosen(service,    "e");
    }
    
    private EntityService mockService() throws SQLException {
        EntityService service = mock(EntityService.class);
        return service;
    }
    
    private Fingerprint mockFingerprint(int particularity, String signature) {
        Fingerprint fp = mock(Fingerprint.class);
        when(fp.getParticularity()).thenReturn(particularity);
        when(fp.getSignature()).thenReturn(signature);
        return fp;
    }

    private void verifyFingeprintWasChosen(EntityService service, String signature) throws SQLException {
        verify(service).findPackageCandidateBySignatureAndVector(eq(signature), any(byte[].class));
    }

    private void verifyFingeprintWasNotChosen(EntityService service, String signature) throws SQLException {
        verify(service, never()).findPackageCandidateBySignatureAndVector(eq(signature), any(byte[].class));
    }
}
