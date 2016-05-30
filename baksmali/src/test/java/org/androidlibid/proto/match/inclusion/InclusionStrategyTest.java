//package org.androidlibid.proto.match.inclusion;
//
//import org.androidlibid.proto.match.inclusion.InclusionStrategy;
//import org.androidlibid.proto.match.inclusion.PackageInclusionCalculator;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.androidlibid.proto.MethodFingerprint;
//import org.androidlibid.proto.ao.FingerprintService;
//import org.androidlibid.proto.match.Evaluation;
//import org.androidlibid.proto.match.Evaluation.Position;
//import org.androidlibid.proto.match.FingerprintMatcher.Result;
//import org.androidlibid.proto.match.MatchingStrategy;
//import org.androidlibid.proto.match.ResultEvaluator;
//import org.junit.Before;
//import org.junit.Test;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.when;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//
///**
// *
// * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
// */
//public class InclusionStrategyTest {
//    
//    private FingerprintService service;
//    private PackageInclusionCalculator calculator;
//    private ResultEvaluator evaluator; 
//    
//    private final String pckgName = "a.b.c";
//    private Map<String, MethodFingerprint> pckgNeedles;
//    
//    private List<MethodFingerprint> methodCandidates;
//   
//    @Before
//    public void setUp() throws SQLException {
//        service    = mock(FingerprintService.class);
//        calculator = mock(PackageInclusionCalculator.class);
//        evaluator  = mock(ResultEvaluator.class);
//        pckgNeedles = new HashMap<String, MethodFingerprint>();
//        
//        methodCandidates = new ArrayList<>();
//        
//        createAndAddFingerprintToNeedleMap(pckgName);
//        MethodFingerprint mthdNeedle1 = createAndAddMethodFingerprint(pckgName, "init",   10,20,30);
//        MethodFingerprint mthdNeedle2 = createAndAddMethodFingerprint(pckgName, "update", 40,50,60);
//
//        MethodFingerprint mthdCand1 = addMethodCandidate("update", 40,50,60);
//        MethodFingerprint mthdCand2 = addMethodCandidate("erase",  70,80,90);
//        
//
//        double lengthNeedle1 = mthdNeedle1.getLength();
//        double lengthNeedle2 = mthdNeedle2.getLength();
//        double sizeNeedle1 = lengthNeedle1 * (1 -  0.9999d);
//        double sizeNeedle2 = lengthNeedle2 * (1 -  0.9999d);
//        
//        when(service.findMethodsByLength(lengthNeedle1, sizeNeedle1)).thenReturn(new ArrayList<MethodFingerprint>());
//        when(service.findMethodsByLength(lengthNeedle2, sizeNeedle2)).thenReturn(methodCandidates);
//
////        MethodFingerprint pckgCand1 = mthdCand1.getParent().getParent();
////        MethodFingerprint pckgCand2 = mthdCand2.getParent().getParent();
//        
////        when(service.getPackageByMethod(mthdCand1)).thenReturn(pckgCand1);
////        when(service.getPackageByMethod(mthdCand2)).thenReturn(pckgCand2);
////        
////        when(service.getPackageHierarchy(pckgCand1)).thenReturn(pckgCand1);
////        when(service.getPackageHierarchy(pckgCand2)).thenReturn(pckgCand2);
//        
////        when(calculator.computeInclusion(pckgCand1.getChildFingerprints(), pckgNeedles.get(pckgName).getChildFingerprints())).thenReturn(20.0d);
////        when(calculator.computeInclusion(pckgCand2.getChildFingerprints(), pckgNeedles.get(pckgName).getChildFingerprints())).thenReturn(0.0d);
////        
////        when(calculator.computeInclusion(pckgNeedles.get(pckgName).getChildFingerprints(), pckgNeedles.get(pckgName).getChildFingerprints())).thenReturn(30.0d);
////        
//        Evaluation eval = new Evaluation();
//        
//        when(evaluator.evaluateResult(any(Result.class))).thenReturn(eval);
//        
//    }
//
//    @Test
//    public void testMatch() throws SQLException {
//        
//        MatchingStrategy strategy = new InclusionStrategy(service, calculator, evaluator);
//        strategy.matchPrints(pckgNeedles);
//        
//        Map<Position, Integer> positions = strategy.getPositions();
//        
//        assert(positions.containsKey(Position.OK));
//        assert(positions.get(Position.OK) == 1);
//        
//        verify(service, times(1)).getPackageByMethod(any(MethodFingerprint.class));
//        
//    }
//    
//    private MethodFingerprint addMethodCandidate(String name, int... values) {
//    
//        MethodFingerprint methodCandidate = new MethodFingerprint("pckg." + name + ":Class:" + name);
//        methodCandidate.setFeatureValues(values);
//        methodCandidates.add(methodCandidate);
//        
//        MethodFingerprint packageCandidate = new MethodFingerprint("pckg." + name);
//        MethodFingerprint classCandidate   = new MethodFingerprint("pckg." + name + ":Class");
//        
//        packageCandidate.addChildFingerprint(classCandidate);
//        classCandidate.addChildFingerprint(methodCandidate);
//        
//        return methodCandidate;
//    }
//    
//    private MethodFingerprint createAndAddMethodFingerprint(String pckgName, String name, int... values) {
//        
//        MethodFingerprint mthd = new MethodFingerprint(name);
//        mthd.setFeatureValues(values);
//        pckgNeedles.get(pckgName).getChildFingerprints().get(0).addChildFingerprint(mthd);
//        
//        return mthd;
//    }
//    
//    private void createAndAddFingerprintToNeedleMap(String pckgName) {
//        MethodFingerprint pckgPrint = new MethodFingerprint(pckgName);
//        MethodFingerprint clssPrint = new MethodFingerprint(pckgName + ":Class");
//        
//        pckgPrint.addChildFingerprint(clssPrint);
//        
//        pckgNeedles.put(pckgPrint.getName(), pckgPrint);
//    }
//
//}
