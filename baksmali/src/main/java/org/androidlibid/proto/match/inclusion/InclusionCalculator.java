package org.androidlibid.proto.match.inclusion;

import java.util.List;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface InclusionCalculator {

    double computeInclusion(List<Fingerprint> superSet, List<Fingerprint> subSet);
    
}
