package org.androidlibid.proto.match;

import org.androidlibid.proto.ao.FingerprintService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.junit.Before;
import org.junit.Test;
import org.la4j.vector.dense.BasicVector;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnMethodLevelWithInclusionStrategyTest {
    
    FingerprintService service; 
    ResultEvaluator    evaluator;   
    FingerprintMatcher matcher;
    Map<String, Fingerprint> appPackagePrints;
    
    @Before 
    public void setUp() throws SQLException {
//        service   = Mockito.mock(FingerprintService.class);
//        evaluator = Mockito.mock(ResultEvaluator.class);
//        matcher   = new FingerprintMatcher(1000);
//        
//        when(evaluator.evaluateResult(any(Fingerprint.class), any(FingerprintMatcher.Result.class)))
//                .thenReturn(MatchingStrategy.Status.OK);
        
        
//        PackageHierarchyGenerator gen = new PackageHierarchyGenerator();
        
        //prepare haystack hierarchy
//        List<Fingerprint> packageHierarchy = gen.generatePackageHierarchy(); 
//        List<Fingerprint> haystackMethods  = new ArrayList<>();
//        
//        for(Fingerprint pckg : packageHierarchy) {
//            haystackMethods.addAll(pckg.getChildren());
//        }
        
//        when(service.findMethodsByPackageDepth(1)).thenReturn(haystackMethods);
//        when(service.findMethodsByLength(any(Double.class), any(Double.class)))
//                .thenReturn(haystackMethods);
//        when(service.getPackageHierarchyByMethod(any(Fingerprint.class))).thenAnswer(getAnswer());
//
//        //Prepare app package hierarchy
//        appPackagePrints = new HashMap<>();
//        Fingerprint needlePackage = new Fingerprint(pckg2Name);
//            
//            Fingerprint needleClass1 = new Fingerprint(class21Name);
//                needleClass1.addChild(method212);
//                needleClass1.addChild(method213);
//                needlePackage.addChild(needleClass1);
//                
//            Fingerprint needleClass2 = new Fingerprint(class22Name);
//                needleClass2.addChild(method222);
//                needleClass2.addChild(method223);
//                needlePackage.addChild(needleClass2);
//        
//        appPackagePrints.put("needlePackage", needlePackage);
    }
    
//    @Test 
    public void testMatching() throws SQLException {
        //TODO this testcase makes no sense right now. MatchOnMethodLevelWithInclusionStrategy has become a dependency clusterf*ck.
//        MatchOnMethodLevelWithInclusionStrategy strategy = new MatchOnMethodLevelWithInclusionStrategy(service, matcher, evaluator);
//        Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(appPackagePrints);
//        assert(stats.get(MatchingStrategy.Status.OK) == 1);
//        System.out.println("Stats: ");
//        for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
//            System.out.println(key.toString() + ": " + stats.get(key));
//        }
    }

    private Answer<?> getAnswer() {
        return new Answer<Fingerprint>() {
            @Override
            public Fingerprint answer(InvocationOnMock invocation) throws Throwable {
                Fingerprint method = (Fingerprint) invocation.getArguments()[0];
                Fingerprint pckg = method.getParent().getParent();
                return pckg;
            }
        };
    }
}
