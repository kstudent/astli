//package org.androidlibid.proto.match.vector;
//
//import org.androidlibid.proto.match.vector.VectorDifferenceStrategy;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.androidlibid.proto.MethodFingerprint;
//import org.androidlibid.proto.ao.FingerprintService;
//import org.androidlibid.proto.match.Evaluation;
//import org.androidlibid.proto.match.Evaluation.Position;
//import org.androidlibid.proto.match.FingerprintMatcher;
//import org.androidlibid.proto.match.MatchingStrategy;
//import org.androidlibid.proto.match.ResultEvaluator;
//import org.androidlibid.proto.match.vector.VectorDifferenceStrategy.Level;
//import org.junit.Before;
//import org.junit.Test;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.times;
//import static org.androidlibid.proto.match.FingerprintMatcher.Result;
//import org.apache.commons.lang.StringUtils;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.verify;
//
//
///**
// *
// * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
// */
//public class VectorDifferenceStrategyTest {
//
//    private FingerprintService service;
//    private FingerprintMatcher matcher;
//    private ResultEvaluator    evaluator;
//    
//    private Map<String, MethodFingerprint> pckgNeedles, clssNeedles, mthdNeedles;
//    private List<MethodFingerprint> pckgsD2, pckgsD3, clsssD2, clsssD3, mthdsD2, mthdsD3;
//
//    @Before
//    @SuppressWarnings("unchecked")
//    public void setUp() throws SQLException {
//        service   = mock(FingerprintService.class);
//        matcher   = mock(FingerprintMatcher.class);
//        evaluator = mock(ResultEvaluator.class);
//
//        pckgsD2 = new ArrayList<>();
//        pckgsD3 = new ArrayList<>();
//        clsssD2 = new ArrayList<>();
//        clsssD3 = new ArrayList<>();
//        mthdsD2 = new ArrayList<>();
//        mthdsD3 = new ArrayList<>();
//        
//        pckgNeedles = new HashMap<>();
//        clssNeedles = new HashMap<>();
//        mthdNeedles = new HashMap<>();
//        
//        for(String needleName : new String[]{"a.b.c", "a.b.c.d", "a.b.d.a"}) {
//            createAndAddFingerprintsToNeedleMaps(needleName); 
//        }
//
//        for(String libName : new String[]{
//            "tld.lib.pckg1", 
//            "tld.lib.pckg2", 
//            "tld.lib.pckg2.subPckgA", 
//            "tld.lib.pckg2.subPckgB"}) 
//        {
//            createAndAddFingerprintsToDepthLists(libName); 
//        }
//        
//        when(service.findPackagesByDepth(2)).thenReturn(pckgsD2);
//        when(service.findPackagesByDepth(3)).thenReturn(pckgsD3);
//        when(service.findClassesByPackageDepth(2)).thenReturn(clsssD2);
//        when(service.findClassesByPackageDepth(3)).thenReturn(clsssD3);
//        when(service.findMethodsByPackageDepth(2)).thenReturn(mthdsD2);
//        when(service.findMethodsByPackageDepth(3)).thenReturn(mthdsD3);
//        
//        when(matcher.matchFingerprints(any(List.class), any(MethodFingerprint.class))).thenAnswer(new Answer<Result>(){
//            @Override
//            @SuppressWarnings("unchecked")
//            public Result answer(InvocationOnMock invocation) throws Throwable {
//                List<MethodFingerprint> hayStack = (List<MethodFingerprint>) invocation.getArguments()[0];
//                MethodFingerprint needle = (MethodFingerprint) invocation.getArguments()[1];
//                Result result = new Result();
//                result.setNeedle(needle);
//                result.setMatchesByDistance(hayStack);
//                return result;
//            }
//        });
//        
//        when(evaluator.evaluateResult(any(Result.class))).thenReturn(new Evaluation());
//
//    }
//    
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testMatchOnPackageLevel() throws SQLException {
//        Level level = Level.PACKAGE;
//                 
//        MatchingStrategy strategy = new VectorDifferenceStrategy(
//            service, evaluator, matcher, level);
//                
//        strategy.matchPrints(pckgNeedles);
//        
//        Map<Position, Integer> stats = strategy.getPositions();
//        
//        verify(matcher, times(3)).matchFingerprints(any(List.class), any(MethodFingerprint.class));
//        
//        assert(stats.get(Position.OK).equals(3));
//    }
//    
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testMatchOnClassLevel() throws SQLException {
//        Level level = Level.CLASS;
//                 
//        MatchingStrategy strategy = new VectorDifferenceStrategy(
//            service, evaluator, matcher, level);
//                
//        strategy.matchPrints(pckgNeedles);
//        Map<Position, Integer> stats = strategy.getPositions();
//        
//        verify(matcher, times(3)).matchFingerprints(any(List.class), any(MethodFingerprint.class));
//        
//        assert(stats.get(Position.OK).equals(3));
//    }
//    
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testMatchOnMethodLevel() throws SQLException {
//        Level level = Level.METHOD;
//                 
//        MatchingStrategy strategy = new VectorDifferenceStrategy(
//            service, evaluator, matcher, level);
//                
//        strategy.matchPrints(pckgNeedles);
//        Map<Position, Integer> stats = strategy.getPositions();
//        
//        verify(matcher, times(3)).matchFingerprints(any(List.class), any(MethodFingerprint.class));
//        
//        assert(stats.get(Position.OK).equals(3));
//    }
//    
//    private void createAndAddFingerprintsToDepthLists(String name) {
//        int depth = StringUtils.countMatches(name, ".");
//        
//        MethodFingerprint pckgPrint = new MethodFingerprint(name);
//        MethodFingerprint clssPrint = new MethodFingerprint(name + ":Class");
//        MethodFingerprint mthdPrint = new MethodFingerprint(name + ":Class:method()");
//
//        pckgPrint.addChildFingerprint(clssPrint);
//        clssPrint.addChildFingerprint(mthdPrint);
//        
//        if(depth == 2) {
//            pckgsD2.add(pckgPrint);
//            clsssD2.add(clssPrint);
//            mthdsD2.add(mthdPrint);
//        } else {
//            pckgsD3.add(pckgPrint);
//            clsssD3.add(clssPrint);
//            mthdsD3.add(mthdPrint);
//        }
//    }
//
//    private void createAndAddFingerprintsToNeedleMaps(String name) {
//        MethodFingerprint pckgPrint = new MethodFingerprint(name);
//        MethodFingerprint clssPrint = new MethodFingerprint(name + ":Class");
//        MethodFingerprint mthdPrint = new MethodFingerprint(name + ":Class:method()");
//        
//        pckgPrint.addChildFingerprint(clssPrint);
//        clssPrint.addChildFingerprint(mthdPrint);
//        
//        pckgNeedles.put(pckgPrint.getName(), pckgPrint);
//        clssNeedles.put(clssPrint.getName(), clssPrint);
//        mthdNeedles.put(mthdPrint.getName(), mthdPrint);
//    }
//}
