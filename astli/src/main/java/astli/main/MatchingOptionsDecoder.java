package astli.main;

import astli.pojo.ASTLIOptions;
import astli.score.HybridMatcher;
import astli.score.InclusionMatcher;
import astli.score.SimilarityMatcher;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchingOptionsDecoder {
    
    /**
     * Prepares options depending on algID
     * 
     * @param algId
     * @param astliOptions (modified!)
     * @return false if algID unknown
     */
    public static boolean decode(String algId, ASTLIOptions astliOptions) {
        
        if (null == algId) {
            return false;
        }
        
        //requires sync with build.gradle (hardcoded! bad! :P)
        switch (algId) {
            case "mat1":
                astliOptions.matcher = SimilarityMatcher.class;
                break;
            case "mat2":
                astliOptions.matcher = HybridMatcher.class;
                break;
            case "mat3":
                astliOptions.matcher = InclusionMatcher.class;
                break;
        }
        
        return false;
    }

}
