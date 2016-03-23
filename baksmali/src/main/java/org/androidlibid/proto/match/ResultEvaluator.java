package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.logger.MyLogger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ResultEvaluator {
    
    private final NumberFormat frmt = new DecimalFormat("#0.00");
    private final FingerprintService service;
    private static final Logger LOG = MyLogger.getLogger( ResultEvaluator.class.getName() );
    
    public ResultEvaluator(FingerprintService service) {
        this.service = service;
    }
    
    public MatchingStrategy.Status evaluateResult(Fingerprint needle, 
            FingerprintMatcher.Result result) {
        
        String needleName = needle.getName();
        Fingerprint nameMatch = result.getMatchByName();
        List<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        
        if(nameMatch == null) {
            try { 
                List<Fingerprint> packagesWithTheSameName = service.findPackageByName(needleName);
                if(packagesWithTheSameName.isEmpty()) {
                    LOG.log(Level.INFO, "{0}: not matched by name", needleName);
                    return MatchingStrategy.Status.NO_MATCH_BY_NAME;
                } else {
                    LOG.log(Level.INFO, "{0}: not matched by name, but its in the db {1} time(s)", new Object[]{needleName, packagesWithTheSameName.size()});
                    return MatchingStrategy.Status.NO_MATCH_BY_NAME_ALTHOUGH_IN_DB;
                }
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, ex.toString(), ex);
                return MatchingStrategy.Status.NO_MATCH_BY_NAME;
            }
        } else {
            
            int position;
            
            for (position = 0; position < matchesByDistance.size(); position++) {
                if(matchesByDistance.get(position).getName().equals(needleName)) {
                    break;
                }
            }
            
            LOG.log(Level.INFO, "* {0} (max : {1}) found at position {2}", new Object[]{needle.getName(), frmt.format(needle.getInclusionScore()), position});
            
            for(int i = 0; i < result.getMatchesByDistance().size(); i++) {
                Fingerprint matchByDistance = result.getMatchesByDistance().get(i);
                LOG.log(Level.FINE, "| {0} | {1} | {2} | {3} |" 
                , new Object[]{i, frmt.format(matchByDistance.getInclusionScore()), frmt.format(matchByDistance.euclideanDiff(needle)), matchByDistance.getName()});
            }
            
            if(position == 0) {
                return MatchingStrategy.Status.OK;
            } else {
                return MatchingStrategy.Status.NOT_PERFECT;
            }
            
            
//            if(position > 0) {
//                System.out.println("--------------------------------------------");
//                System.out.println(needle.getName() + " not perfectly matched");
//                
//                System.out.println("euc. diff: " + frmt.format(needle.euclideanDiff(nameMatch)) 
//                        + "; incScore: " +  frmt.format(nameMatch.getInclusionScore()) 
//                        + "; position: " + position);
//                
//                int maxShow = ((position + 20) < matchesByDistance.size()) ? position + 20 : matchesByDistance.size();
//                
//                System.out.println("other matches (" + maxShow + "):");
//                for(int j = 0; j < maxShow; j++) {
//                    System.out.print(matchesByDistance.get(j).getName() + " ("
//                            + frmt.format(needle.euclideanDiff(matchesByDistance.get(j))) + ")");
//                    
//                    double score = matchesByDistance.get(j).getInclusionScore();
//                    
//                    if(score > 0.0d) {
//                        System.out.println(" (" + frmt.format(score) + ")"); 
//                    } else {
//                        System.out.println(); 
//                    }
//                }
//                
//                if(position == matchesByDistance.size()) {
//                    System.out.println(needleName + ": not mached by distance.");
//                    System.out.println("--------------------------------------------");
//                    return MatchingStrategy.Status.NO_MATCH_BY_DISTANCE;
//                } else {
//                    System.out.println(needleName + ": found at position " + (position + 1));
//                    System.out.println("--------------------------------------------");
//                    return MatchingStrategy.Status.NOT_PERFECT;
//                } 
//            } else {
//                double diff = needle.euclideanDiff(nameMatch);
//                System.out.println(needleName + ": machted correctly with incScore: " + frmt.format(nameMatch.getInclusionScore()) );
//                System.out.print("    Diff to next in lines: " );
//                
//                int counter = 0;
//                for (Fingerprint matchByDistance : matchesByDistance) {
//                    System.out.print(frmt.format(matchByDistance.getInclusionScore()) + ", ");
//                    if(counter++ > 10) break;
//                } 
//                System.out.print("\n");
//                
//                return MatchingStrategy.Status.OK;
//            }
        }
    }
    
}
