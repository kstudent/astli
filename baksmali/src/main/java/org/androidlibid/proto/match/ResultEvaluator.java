package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ResultEvaluator {
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");  
    
    public MatchingStrategy.Status evaluateResult(Fingerprint needle, 
            FingerprintMatcher.Result result) {
        
        String needleName = needle.getName();
        Fingerprint nameMatch = result.getMatchByName();
        List<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        
        if(nameMatch == null) {
            System.out.println(needleName + ": not mached by name");
            return MatchingStrategy.Status.NO_MATCH_BY_NAME;
        } else {
            
            int position;
            
            for (position = 0; position < matchesByDistance.size(); position++) {
                if(matchesByDistance.get(position).getName().equals(needleName)) {
                    break;
                }
            }
            
            if(position > 0) {
                System.out.println("--------------------------------------------");
                System.out.println("Needle: ");
                System.out.println(needle);
                
                System.out.println("Match By Name: ");
                System.out.println(nameMatch);
                
                System.out.println("euc. diff: " + frmt.format(needle.euclideanDiff(nameMatch)) + "; in betweeners: " + position);
                
//                System.out.println("closer matches:");
//                for(int j = 0; j < position; j++) {
//                    System.out.println(matchesByDistance.get(j).getName() + " ("
//                            + frmt.format(needle.euclideanDiff(matchesByDistance.get(j))) + ")");
//                }
                
                if(position == matchesByDistance.size()) {
                    System.out.println(needleName + ": not mached by distance.");
                    System.out.println("--------------------------------------------");
                    return MatchingStrategy.Status.NO_MATCH_BY_DISTANCE;
                } else {
                    System.out.println(needleName + ": found at position " + (position + 1));
                    System.out.println("--------------------------------------------");
                    return MatchingStrategy.Status.NOT_PERFECT;
                } 
            } else {
                double diff = needle.euclideanDiff(nameMatch);
                System.out.println(needleName + ": machted correctly with diff: " + frmt.format(diff) );
                System.out.print("    Diff to next in lines: " );
                
                int counter = 0;
                for (Fingerprint matchByDistance : matchesByDistance) {
                    System.out.print(frmt.format(needle.euclideanDiff(matchByDistance)) + ", ");
                    if(counter++ > 10) break;
                } 
                System.out.print("\n");
                
                return MatchingStrategy.Status.OK;
            }
        }
    }
    
}
