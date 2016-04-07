package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchWithVectorDifferenceStrategyTest {

    private FingerprintService service;
    private FingerprintMatcher matcher;
    private Map<String, Fingerprint> needles;
    private Fingerprint libPckg1, libPckg2, libPckg3, libPckg4;
    
    @Before
    public void setUp() throws SQLException {
        service   = mock(FingerprintService.class);
        matcher   = mock(FingerprintMatcher.class);

        List<Fingerprint> packagesWithDepth2 = new ArrayList<>();
        List<Fingerprint> packagesWithDepth3 = new ArrayList<>();
        
        libPckg1 = addPrintToList(packagesWithDepth2, "tld.lib.pckg1",          1, 2,  3); 
        libPckg2 = addPrintToList(packagesWithDepth2, "tld.lib.pckg2",          4, 5,  6); 
        libPckg3 = addPrintToList(packagesWithDepth3, "tld.lib.pckg2.subPckgA", 7, 8,  9);
        libPckg4 = addPrintToList(packagesWithDepth3, "tld.lib.pckg2.subPckgB", 7, 8, 10);
        
        when(service.findPackagesByDepth(2)).thenReturn(packagesWithDepth2);
        when(service.findPackagesByDepth(3)).thenReturn(packagesWithDepth3);
        
        needles = new HashMap<>();
        Fingerprint needle0 = addPrintToMap(needles, "a.b.c",   1, 2, 3); 
        Fingerprint needle1 = addPrintToMap(needles, "a.b.d",   4, 5, 6); 
        Fingerprint needle2 = addPrintToMap(needles, "a.b.d.a", 7, 8, 9);
        
        when(matcher.matchFingerprints(packagesWithDepth2, needle0))
                .thenReturn(createResult(needle0, null, libPckg1, libPckg2));
        
        when(matcher.matchFingerprints(packagesWithDepth2, needle1))
                .thenReturn(createResult(needle1, null, libPckg2, libPckg1));
        
        when(matcher.matchFingerprints(packagesWithDepth3, needle2))
                .thenReturn(createResult(needle2, null, libPckg3, libPckg4));
        
    }
    
    @Test
    public void testMatchOnPackageLevel() throws SQLException {
        Level level = Level.PACKAGE;
        
        ResultEvaluator evaluator = new ResultEvaluator() {
            @Override
            public MatchingStrategy.Status evaluateResult(Fingerprint needle, Result result) {
                
                if(needle.equals(needles.get("a.b.c"))) {
                    assert(result.getMatchesByDistance().get(0).equals(libPckg1));
                } else if(needle.equals(needles.get("a.b.d"))) {
                    assert(result.getMatchesByDistance().get(0).equals(libPckg2));
                } else if(needle.equals(needles.get("a.b.d.a"))) {
                    assert(result.getMatchesByDistance().get(0).equals(libPckg3));
                } else {
                    assert(false);
                }
                return MatchingStrategy.Status.OK;
            }
        };
        
        MatchingStrategy strategy = new MatchWithVectorDifferenceStrategy(
            service, evaluator, matcher, level);
                
        Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(needles);
        
        assert(stats.get(MatchingStrategy.Status.OK).equals(3));
        
    }
    
    private Fingerprint addPrintToList(List<Fingerprint> list, String name, int... values) {
        Fingerprint print = new Fingerprint(name);
        print.setFeatureValues(values);
        list.add(print);
        return print;
    }

    private Fingerprint addPrintToMap(Map<String, Fingerprint> map, String name, int... values) {
        Fingerprint print = new Fingerprint(name);
        print.setFeatureValues(values);
        map.put(name, print);
        return print;
    }

    private Result createResult(Fingerprint needle, Fingerprint matchByName, Fingerprint... matchesByDistance) {
        Result result = new Result();
        result.setNeedle(needle);
        result.setMatchByName(matchByName);
        result.setMatchesByDistance(Arrays.asList(matchesByDistance));
        
        return result;
    }
}
