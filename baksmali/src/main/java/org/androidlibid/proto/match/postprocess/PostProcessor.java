package org.androidlibid.proto.match.postprocess;

import org.androidlibid.proto.match.MatchingProcess;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface PostProcessor {
    
    void process(MatchingProcess.Result result);
    void done();
    
}
