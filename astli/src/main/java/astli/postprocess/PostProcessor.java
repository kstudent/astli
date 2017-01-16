package astli.postprocess;

import astli.pojo.Match;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public interface PostProcessor {
    
    void init(); 
    void process(Match match);
    void done(int totalPackages, int keptPackages);
    
}
