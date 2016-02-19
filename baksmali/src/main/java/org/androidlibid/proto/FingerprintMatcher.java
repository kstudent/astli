package org.androidlibid.proto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.Class;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcher {


    EntityService service; 
    private final double diffThreshold = 100;

    public FingerprintMatcher(EntityService service) {
        this.service = service;
    }
    
    public FingerprintMatcherResult matchFingerprints(final Fingerprint needle) throws SQLException {
        
        List<Fingerprint> matches = new ArrayList<>();
        FingerprintMatcherResult result = new FingerprintMatcherResult();
        
        for(Class candidateEntity : service.getClassFingerprintEntities()) {
            
            Fingerprint candidate = new Fingerprint(candidateEntity);
            
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
        
        return result; 
    }
    
    public static class FingerprintMatcherResult {
        private List<Fingerprint> matchesByDistance;
        @Nullable
        private Fingerprint matchByName;

        public FingerprintMatcherResult() {
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
    }
}
