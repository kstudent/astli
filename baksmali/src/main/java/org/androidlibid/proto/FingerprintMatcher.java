package org.androidlibid.proto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.androidlibid.proto.ao.FingerprintEntity;
import org.androidlibid.proto.ao.FingerprintService;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcher {


    FingerprintService service; 
    private final double diffThreshold = 100000; 

    public FingerprintMatcher(FingerprintService service) {
        this.service = service;
    }
    
    public List<Fingerprint> matchFingerprints(final Fingerprint needle) {
        
        List<Fingerprint> matches = new ArrayList<>();
        
        for(FingerprintEntity candidateEntity : service.getFingerprintEntities()) {
            
            Fingerprint candidate = new Fingerprint(candidateEntity);
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
        
        return matches; 
    }
}
