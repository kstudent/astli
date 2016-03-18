package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ao.Package;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnClassLevelStrategy implements MatchingStrategy {

    private final EntityService service;
    private final ResultEvaluator evaluator;
    private final FingerprintMatcher matcher;

    public MatchOnClassLevelStrategy(EntityService service, ResultEvaluator evaluator, FingerprintMatcher matcher) {
        this.service = service;
        this.evaluator = evaluator;
        this.matcher = matcher;
    }

    @Override
    public Map<Status, Integer> matchPrints(Map<String, Fingerprint> packagePrints) throws SQLException {
        
        Map<MatchingStrategy.Status, Integer> stats = new HashMap<>();
        for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
            stats.put(key, 0);
        }
        
        for(Fingerprint packageNeedle : packagePrints.values()) {
            
            if(packageNeedle.getName().startsWith("android")) continue;
            if(packageNeedle.getName().equals("")) continue;
            
            int level = StringUtils.countMatches(packageNeedle.getName(), ".");
            
            List<Fingerprint> haystack = new ArrayList<>();
            
            for (Package pckg : service.findPackagesByDepth(level)) {
                for(VectorEntity v : pckg.getClasses()) {
                    haystack.add(new Fingerprint(v));
                }
            }

            for (Fingerprint needle : packageNeedle.getChildren()) {
                FingerprintMatcher.Result matches = matcher.matchFingerprints(haystack, needle);
                MatchingStrategy.Status result = evaluator.evaluateResult(needle, matches);
                stats.put(result, stats.get(result) + 1);
            }
        }
        
        return stats;

    }
    
}
