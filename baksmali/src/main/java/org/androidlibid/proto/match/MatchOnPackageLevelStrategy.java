package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.VectorEntity;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnPackageLevelStrategy implements MatchingStrategy {

    private final EntityService service;
    private final ResultEvaluator evaluator;
    private final FingerprintMatcher matcher;

    public MatchOnPackageLevelStrategy(EntityService service, ResultEvaluator evaluator, FingerprintMatcher matcher) {
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
        
        List<VectorEntity> haystackEntities = new ArrayList<VectorEntity>(service.findPackages());
        List<Fingerprint>  haystack  = new ArrayList<>(haystackEntities.size());
        for(VectorEntity v : haystackEntities) {
            haystack.add(new Fingerprint(v));
        }
        
        for(Fingerprint needle : packagePrints.values()) {
            
            if(needle.getName().startsWith("android")) continue;
            if(needle.getName().equals("")) continue;
        
            FingerprintMatcher.Result matches = matcher.matchFingerprints(haystack, needle);
            Status result = evaluator.evaluateResult(needle, matches);
            stats.put(result, stats.get(result) + 1);
        }
        
        return stats;
    }
    
}
