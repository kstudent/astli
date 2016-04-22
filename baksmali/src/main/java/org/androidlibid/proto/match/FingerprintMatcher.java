package org.androidlibid.proto.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcher {

    private final double simThreshold;

    /**
     * Create Matcher without threshold.
     */
    public FingerprintMatcher() {
        this.simThreshold = -1.0d;
    }
    
    /**
     * Create Matcher with threshold.
     * 
     * @param simThreshold Specifies whether or not a candidate is "similar" 
     * enough. Values: (0.0d - 1.0d)
     */
    public FingerprintMatcher(double simThreshold) {
        
        if(simThreshold < 0.0d || simThreshold > 1.0d) {
            throw new RuntimeException("Threshold needs to be within [0, 1]");
        }
        
        this.simThreshold = simThreshold;
    }
    
    public Result matchFingerprints(final List<Fingerprint> haystack, final Fingerprint needle) {
        
        List<Fingerprint> matches = new ArrayList<>();
        Result result = new Result();
        
        double maxSimScore = needle.getLength();
        needle.setComputedSimilarityScore(maxSimScore);
        
        for(Fingerprint candidate : haystack) {

            if(candidate.getName() == null) {
                throw new RuntimeException("Vector name is null...");
            }

            if(candidate.getName().equals(needle.getName())) {
                result.setMatchByName(candidate);
            }

            double simScore = needle.getNonCommutativeSimilarityScoreToFingerprint(candidate);
            
            if(simThreshold > 0.0d) {
                double simScoreNormalized = simScore / maxSimScore;
                if(simScoreNormalized >= simThreshold) {
                    matches.add(candidate);
                    candidate.setComputedSimilarityScore(simScore);
                }
            } else {
                matches.add(candidate);
                candidate.setComputedSimilarityScore(simScore);
            }
        }
                
        Collections.sort(matches, new Comparator<Fingerprint>() {
            @Override
            public int compare(Fingerprint that, Fingerprint other) {
                double diffNeedleThat  = needle.getDistanceToFingerprint(that);
                double diffNeedleOther = needle.getDistanceToFingerprint(other);
                if (diffNeedleThat > diffNeedleOther) return  1;
                if (diffNeedleThat < diffNeedleOther) return -1;
                return 0;
            }
        }); 
        
        result.setMatchesByDistance(matches);
        result.setNeedle(needle);
        
        return result; 
    }
    
    public static class Result {
        private Collection<Fingerprint> matchesByDistance;
        @Nullable
        private Fingerprint matchByName;
        private Fingerprint needle;
        
        public Result() {
        }

        public Collection<Fingerprint> getMatchesByDistance() {
            return matchesByDistance;
        }

        public void setMatchesByDistance(Collection<Fingerprint> matchesByDistance) {
            this.matchesByDistance = matchesByDistance;
        }

        public Fingerprint getMatchByName() {
            return matchByName;
        }

        public void setMatchByName(Fingerprint matchByName) {
            this.matchByName = matchByName;
        }

        public Fingerprint getNeedle() {
            return needle;
        }

        public void setNeedle(Fingerprint needle) {
            this.needle = needle;
        }
    }
}
