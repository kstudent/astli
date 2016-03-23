package org.androidlibid.proto.match;

import java.util.ArrayList;
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

    private final double diffThreshold;

    public FingerprintMatcher(double diffThreshold) {
        this.diffThreshold = diffThreshold;
    }
    
    public Result matchFingerprints(final List<Fingerprint> haystack, final Fingerprint needle) {
        
        List<Fingerprint> matches = new ArrayList<>();
        Result result = new Result();
        
        for(Fingerprint candidate : haystack) {

            if(candidate.getName() == null) {
                throw new RuntimeException("Database contains vector with name == null... why?");
            }

            if(candidate.getName().equals(needle.getName())) {
                result.setMatchByName(candidate);
            }

            if(needle.euclideanDiff(candidate) < diffThreshold) {
                matches.add(candidate);
            }  

        }
                
        Collections.sort(matches, new Comparator<Fingerprint>() {
            @Override
            public int compare(Fingerprint that, Fingerprint other) {
                double diffNeedleThat  = needle.euclideanDiff(that);
                double diffNeedleOther = needle.euclideanDiff(other);
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
        private List<Fingerprint> matchesByDistance;
        @Nullable
        private Fingerprint matchByName;
        private Fingerprint needle;
        
        public Result() {
        }

        public List<Fingerprint> getMatchesByDistance() {
            return matchesByDistance;
        }

        public void setMatchesByDistance(List<Fingerprint> matchesByDistance) {
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
