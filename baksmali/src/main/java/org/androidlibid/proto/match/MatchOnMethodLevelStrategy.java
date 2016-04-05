package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.Clazz;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.Method;
import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ao.Package;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnMethodLevelStrategy implements MatchingStrategy {

    private final EntityService service;
    private final ResultEvaluator evaluator;
    private final FingerprintMatcher matcher;

    public MatchOnMethodLevelStrategy(EntityService service, ResultEvaluator evaluator, FingerprintMatcher matcher) {
        this.service = service;
        this.evaluator = evaluator;
        this.matcher = matcher;
    }

    @Override
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        Map<Status, Integer> stats = new HashMap<>();
        for(Status key : Status.values()) {
            stats.put(key, 0);
        }
        
        int count = 0;
        
        for(Fingerprint packageNeedle : packagePrints.values()) {
            
            System.out.println(((float)(count++) / packagePrints.size()) * 100 + "%"); 
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
         
            int level = StringUtils.countMatches(packageNeedle.getName(), ".");
            
            List<Fingerprint> haystack = new ArrayList<>();
            
            for (Package pckg : service.findPackagesByDepth(level)) {
                for(VectorEntity clazz : pckg.getClazzes()) {
                    Clazz clazz1 = (Clazz) clazz;
                    for (Method m : clazz1.getMethods()) {
                        haystack.add(new Fingerprint(m));
                    }
                }
            }
            
            for(Fingerprint classNeedle : packageNeedle.getChildFingerprints()) {
                for(Fingerprint methodNeedle : classNeedle.getChildFingerprints()) {
                    FingerprintMatcher.Result matches = matcher.matchFingerprints(haystack, methodNeedle);
                    Status result = evaluator.evaluateResult(methodNeedle, matches);
                    stats.put(result, stats.get(result) + 1);
                }
            }
        }
        
        return stats;
        
    }
    
}
