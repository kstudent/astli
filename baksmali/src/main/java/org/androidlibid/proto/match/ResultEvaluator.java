package org.androidlibid.proto.match;

import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
interface ResultEvaluator {
    
    public Evaluation evaluateResult(Result result);
}
