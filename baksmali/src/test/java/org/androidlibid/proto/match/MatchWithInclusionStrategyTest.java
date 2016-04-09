package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.match.FingerprintMatcher.Result;
import org.androidlibid.proto.match.MatchingStrategy.Status;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchWithInclusionStrategyTest {
    
    private FingerprintService service;
    private PackageInclusionCalculator calculator;
    private ResultEvaluator evaluator; 
    
    private final String pckgName = "a.b.c";
    private Map<String, Fingerprint> pckgNeedles;
    
    private List<Fingerprint> methodCandidates;
    
    @Before
    public void setUp() throws SQLException {
        service    = mock(FingerprintService.class);
        calculator = mock(PackageInclusionCalculator.class);
        evaluator  = mock(ResultEvaluator.class);
        pckgNeedles = new HashMap<String, Fingerprint>();
        
        methodCandidates = new ArrayList<>();
        
        createAndAddFingerprintToNeedleMap(pckgName);
        Fingerprint mthdNeedle1 = createAndAddMethodFingerprint(pckgName, "init",   10,20,30);
        Fingerprint mthdNeedle2 = createAndAddMethodFingerprint(pckgName, "update", 40,50,60);

        Fingerprint mthdCand1 = addMethodCandidate("update", 40,50,60);
        Fingerprint mthdCand2 = addMethodCandidate("erase",  70,80,90);
        

        double lengthNeedle1 = mthdNeedle1.getLength();
        double lengthNeedle2 = mthdNeedle2.getLength();
        double sizeNeedle1 = lengthNeedle1 * (1 -  0.9999d);
        double sizeNeedle2 = lengthNeedle2 * (1 -  0.9999d);
        
        when(service.findMethodsByLength(lengthNeedle1, sizeNeedle1)).thenReturn(new ArrayList<Fingerprint>());
        when(service.findMethodsByLength(lengthNeedle2, sizeNeedle2)).thenReturn(methodCandidates);

        Fingerprint pckgCand1 = mthdCand1.getParent().getParent();
        Fingerprint pckgCand2 = mthdCand2.getParent().getParent();
        
        when(service.getPackageHierarchyByMethod(mthdCand1)).thenReturn(pckgCand1);
        when(service.getPackageHierarchyByMethod(mthdCand2)).thenReturn(pckgCand2);
        
        when(calculator.computePackageInclusion(pckgCand1.getChildFingerprints(), pckgNeedles.get(pckgName).getChildFingerprints())).thenReturn(20.0d);
        when(calculator.computePackageInclusion(pckgCand2.getChildFingerprints(), pckgNeedles.get(pckgName).getChildFingerprints())).thenReturn(0.0d);
        
        when(calculator.computePackageInclusion(pckgNeedles.get(pckgName).getChildFingerprints(), pckgNeedles.get(pckgName).getChildFingerprints())).thenReturn(30.0d);
        
        when(evaluator.evaluateResult(any(Fingerprint.class), any(Result.class))).thenReturn(Status.OK);
        
    }
    
    @Test
    public void testMatch() throws SQLException {
        
        MatchingStrategy strategy;
        strategy = new MatchWithInclusionStrategy(service, calculator, evaluator);
        Map<MatchingStrategy.Status, Integer> matches = strategy.matchPrints(pckgNeedles);
        
        assert(matches.containsKey(Status.OK));
        assert(matches.get(Status.OK) == 1);
        
        verify(service, times(1)).getPackageHierarchyByMethod(any(Fingerprint.class));
        
    }
    
    private Fingerprint addMethodCandidate(String name, int... values) {
    
        Fingerprint methodCandidate = new Fingerprint("pckg." + name + ":Class:" + name);
        methodCandidate.setFeatureValues(values);
        methodCandidates.add(methodCandidate);
        
        Fingerprint packageCandidate = new Fingerprint("pckg." + name);
        Fingerprint classCandidate   = new Fingerprint("pckg." + name + ":Class");
        
        packageCandidate.addChildFingerprint(classCandidate);
        classCandidate.addChildFingerprint(methodCandidate);
        
        return methodCandidate;
    }
    
    private Fingerprint createAndAddMethodFingerprint(String pckgName, String name, int... values) {
        
        Fingerprint mthd = new Fingerprint(name);
        mthd.setFeatureValues(values);
        pckgNeedles.get(pckgName).getChildFingerprints().get(0).addChildFingerprint(mthd);
        
        return mthd;
    }
    
    private void createAndAddFingerprintToNeedleMap(String pckgName) {
        Fingerprint pckgPrint = new Fingerprint(pckgName);
        Fingerprint clssPrint = new Fingerprint(pckgName + ":Class");
        
        pckgPrint.addChildFingerprint(clssPrint);
        
        pckgNeedles.put(pckgPrint.getName(), pckgPrint);
    }
    
    
}
