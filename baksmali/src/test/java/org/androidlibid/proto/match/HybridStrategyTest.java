package org.androidlibid.proto.match;

import org.androidlibid.proto.match.postprocess.ResultClassifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.ao.FingerprintEntity;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.utils.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HybridStrategyTest {

    private FingerprintService fpService;
    private PackageHierarchy hierarchy; 
    private ResultClassifier eval;
    
    @Before 
    public void setUp() {
        fpService = Mockito.mock(FingerprintService.class);
        eval = Mockito.mock(ResultClassifier.class);
        hierarchy = new PackageHierarchy("pckg");
        
        Map<String, Fingerprint> classAPrints = new HashMap<>();
        Map<String, Fingerprint> classBPrints = new HashMap<>();
        classAPrints.put("methodA", createPrint(":V",   0, 1, 2));
        classAPrints.put("methodB", createPrint("I:V",  1, 2, 3));
        classAPrints.put("methodC", createPrint("[O:V", 2, 3, 3));
        classBPrints.put("methodD", createPrint(":V",   0, 1, 2));
        classBPrints.put("methodE", createPrint(":V",   0, 0, 100));
        
        hierarchy.addMethods("classA", classAPrints);
        hierarchy.addMethods("classB", classBPrints);
        
    }
    
    @Test
    public void testMethodDestillation() {
//        MatchingProcess strategy = new MatchingProcess(fpService, 0, eval);
//        
//        List<Fingerprint> methods = strategy.distillMethodsWithHighEntropy(hierarchy)
//                .collect(Collectors.toList());
//        
//        assert(methods.size() == 5);
//        IntStream.range(0, methods.size() - 1).forEach(i -> {
//            assert(methods.get(i).getEntropy() >= methods.get(i+1).getEntropy());
//        });
    }

    private Fingerprint createPrint(String signature, int... values) {
        
        FingerprintEntity mock = Mockito.mock(FingerprintEntity.class);
        Mockito.when(mock.getSignature()).thenReturn(signature);
        Mockito.when(mock.getVector()).thenReturn(ArrayUtils.truncateIntToLEByteArray(values));
        Mockito.when(mock.getName()).thenReturn("");
        
        return new Fingerprint(mock);
    }
    
}
