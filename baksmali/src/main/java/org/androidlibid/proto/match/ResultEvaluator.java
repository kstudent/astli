package org.androidlibid.proto.match;

import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
interface ResultEvaluator {
    
    public MatchingStrategy.Status evaluateResult(
            Fingerprint needle, FingerprintMatcher.Result result);
}
