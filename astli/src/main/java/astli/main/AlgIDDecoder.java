package astli.main;

import astli.find.FindByNeedle;
import astli.pojo.ASTLIOptions;
import astli.score.HybridMatcher;
import astli.score.InclusionMatcher;
import astli.score.SimilarityMatcher;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class AlgIDDecoder {
    
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
            case "simmatch":
                astliOptions.matcher = SimilarityMatcher.class;
                astliOptions.finder = FindByNeedle.class;
                break;
            case "hybmatch":
                astliOptions.matcher = HybridMatcher.class;
                astliOptions.finder = FindByNeedle.class;
                break;
            case "incmatch":
                astliOptions.matcher = InclusionMatcher.class;
                astliOptions.finder = FindByNeedle.class;
                break;
            case "pat" : 
                astliOptions.matcher = SimilarityMatcher.class;
                astliOptions.finder = FindByNeedle.class;
                astliOptions.packageAcceptanceThreshold = -1.0d;
                astliOptions.minimumPackageParticularity = 0;
                break;
            case "minpp" : 
                astliOptions.matcher = SimilarityMatcher.class;
                astliOptions.finder = FindByNeedle.class;
                astliOptions.minimumPackageParticularity = 0;
                break;
            default: 
                return false; 
        }
        
        return true;
    }

}
