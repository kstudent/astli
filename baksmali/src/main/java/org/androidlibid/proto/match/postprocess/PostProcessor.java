package org.androidlibid.proto.match.postprocess;

import org.androidlibid.proto.pojo.Match;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface PostProcessor {
    
    void init(); 
    void process(Match match);
    void done();
    
}
