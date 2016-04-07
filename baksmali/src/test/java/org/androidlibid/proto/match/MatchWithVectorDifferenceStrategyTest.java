package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.match.MatchWithVectorDifferenceStrategy.Level;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;
import org.androidlibid.proto.match.MatchingStrategy.Status;
import org.apache.commons.lang.StringUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchWithVectorDifferenceStrategyTest {

    private FingerprintService service;
    private FingerprintMatcher matcher;
    private ResultEvaluator    evaluator;
    
    private Map<String, Fingerprint> pckgNeedles, clssNeedles, mthdNeedles;
    private List<Fingerprint> pckgsD2, pckgsD3, clsssD2, clsssD3, mthdsD2, mthdsD3;

    @Before
    public void setUp() throws SQLException {
        service   = mock(FingerprintService.class);
        matcher   = mock(FingerprintMatcher.class);
        evaluator = mock(ResultEvaluator.class);

        pckgsD2 = new ArrayList<>();
        pckgsD3 = new ArrayList<>();
        clsssD2 = new ArrayList<>();
        clsssD3 = new ArrayList<>();
        mthdsD2 = new ArrayList<>();
        mthdsD3 = new ArrayList<>();
        
        pckgNeedles = new HashMap<>();
        clssNeedles = new HashMap<>();
        mthdNeedles = new HashMap<>();
        
        for(String needleName : new String[]{"a.b.c", "a.b.c.d", "a.b.d.a"}) {
            createAndAddFingerprintsToNeedleMaps(needleName); 
        }

        for(String libName : new String[]{
            "tld.lib.pckg1", 
            "tld.lib.pckg2", 
            "tld.lib.pckg2.subPckgA", 
            "tld.lib.pckg2.subPckgB"}) 
        {
            createAndAddFingerprintsToDepthLists(libName); 
        }
        
        when(service.findPackagesByDepth(2)).thenReturn(pckgsD2);
        when(service.findPackagesByDepth(3)).thenReturn(pckgsD3);
        when(service.findClassesByPackageDepth(2)).thenReturn(clsssD2);
        when(service.findClassesByPackageDepth(3)).thenReturn(clsssD3);
        when(service.findMethodsByPackageDepth(2)).thenReturn(mthdsD2);
        when(service.findMethodsByPackageDepth(3)).thenReturn(mthdsD3);
        
        when(matcher.matchFingerprints(any(List.class), any(Fingerprint.class))).thenAnswer(new Answer<Result>(){
            @Override
            public Result answer(InvocationOnMock invocation) throws Throwable {
                List<Fingerprint> hayStack = (List<Fingerprint>) invocation.getArguments()[0];
                Fingerprint needle = (Fingerprint) invocation.getArguments()[1];
                Result result = new Result();
                result.setNeedle(needle);
                result.setMatchesByDistance(hayStack);
                return result;
            }
        });
        
        when(evaluator.evaluateResult(any(Fingerprint.class), any(Result.class))).thenReturn(Status.OK);

    }
    
    @Test
    public void testMatchOnPackageLevel() throws SQLException {
        Level level = Level.PACKAGE;
                 
        MatchingStrategy strategy = new MatchWithVectorDifferenceStrategy(
            service, evaluator, matcher, level);
                
        Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(pckgNeedles);
        
        verify(matcher, times(3)).matchFingerprints(any(List.class), any(Fingerprint.class));
        
        assert(stats.get(MatchingStrategy.Status.OK).equals(3));
    }
    
    @Test
    public void testMatchOnClassLevel() throws SQLException {
        Level level = Level.CLASS;
                 
        MatchingStrategy strategy = new MatchWithVectorDifferenceStrategy(
            service, evaluator, matcher, level);
                
        Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(pckgNeedles);
        
        verify(matcher, times(3)).matchFingerprints(any(List.class), any(Fingerprint.class));
        
        assert(stats.get(MatchingStrategy.Status.OK).equals(3));
    }
    
    @Test
    public void testMatchOnMethodLevel() throws SQLException {
        Level level = Level.METHOD;
                 
        MatchingStrategy strategy = new MatchWithVectorDifferenceStrategy(
            service, evaluator, matcher, level);
                
        Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(pckgNeedles);
        
        verify(matcher, times(3)).matchFingerprints(any(List.class), any(Fingerprint.class));
        
        assert(stats.get(MatchingStrategy.Status.OK).equals(3));
    }
    
    private void createAndAddFingerprintsToDepthLists(String name) {
        int depth = StringUtils.countMatches(name, ".");
        
        Fingerprint pckgPrint = new Fingerprint(name);
        Fingerprint clssPrint = new Fingerprint(name + ":Class");
        Fingerprint mthdPrint = new Fingerprint(name + ":Class:method()");

        pckgPrint.addChildFingerprint(clssPrint);
        clssPrint.addChildFingerprint(mthdPrint);
        
        if(depth == 2) {
            pckgsD2.add(pckgPrint);
            clsssD2.add(clssPrint);
            mthdsD2.add(mthdPrint);
        } else {
            pckgsD3.add(pckgPrint);
            clsssD3.add(clssPrint);
            mthdsD3.add(mthdPrint);
        }
    }

    private void createAndAddFingerprintsToNeedleMaps(String name) {
        Fingerprint pckgPrint = new Fingerprint(name);
        Fingerprint clssPrint = new Fingerprint(name + ":Class");
        Fingerprint mthdPrint = new Fingerprint(name + ":Class:method()");
        
        pckgPrint.addChildFingerprint(clssPrint);
        clssPrint.addChildFingerprint(mthdPrint);
        
        pckgNeedles.put(pckgPrint.getName(), pckgPrint);
        clssNeedles.put(clssPrint.getName(), clssPrint);
        mthdNeedles.put(mthdPrint.getName(), mthdPrint);
    }
}
