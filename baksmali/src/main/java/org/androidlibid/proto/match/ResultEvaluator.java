package org.androidlibid.proto.match;

import static org.androidlibid.proto.match.MatchingStrategy.Status;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
interface ResultEvaluator {
    
    public Status evaluateResult(Result result);
}
