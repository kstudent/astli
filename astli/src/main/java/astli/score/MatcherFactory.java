package astli.score;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatcherFactory {

    public static PackageMatcher createMatcher(Class<? extends PackageMatcher> matcher) {
        
        if(matcher.equals(SimilarityMatcher.class)) {
            return new SimilarityMatcher();
        } else if (matcher.equals(HybridMatcher.class)) {
            return new HybridMatcher();
        } else if (matcher.equals(InclusionMatcher.class)) {
            return new InclusionMatcher();
        } 
        throw new UnsupportedOperationException("Matcher not found."); 
    }


}
