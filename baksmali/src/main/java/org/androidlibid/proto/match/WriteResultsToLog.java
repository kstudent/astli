package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.androidlibid.proto.match.MatchingStrategy.Status;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class WriteResultsToLog implements ResultEvaluator {
    
    private final NumberFormat  frmt = new DecimalFormat("#0.00");
    private static final Logger DETAILLOGGER = LogManager.getLogger(WriteResultsToLog.class.getName());
    private static final Logger RESULTLOGGER = LogManager.getLogger(WriteResultsToLog.class.getName() + ".Results");
    
    @Override
    public Status evaluateResult(Result result) {
        
        Fingerprint needle = result.getNeedle();
        Fingerprint nameMatch = result.getMatchByName();
        Collection<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        String needleName = needle.getName();
        
        Status status = Status.NO_MATCH_BY_NAME;
        
        int position = -1;
        
        if(nameMatch == null) {
            DETAILLOGGER.info("{}: not matched by name", needleName);
        } else {
            position = findPosition(needleName, matchesByDistance);
            
            if(position > matchesByDistance.size()) {
                DETAILLOGGER.info("* NEXT {} (max : {}) not found", needle.getName(), needle.getInclusionScore());
                status = Status.NOT_IN_CANDIDATES;
                position = -1;
            } else if( position > 1 ) {
                DETAILLOGGER.info("* NEXT {} (max : {}) found at position {}", 
                        needle.getName(), 
                        frmt.format(needle.getInclusionScore()), 
                        position);
                status = Status.NOT_FIRST;
            } else {
                DETAILLOGGER.info("* {} (max : {}) found position 1", 
                        needle.getName(), 
                        frmt.format(needle.getInclusionScore()), 
                        position);
                status = Status.OK;
            }
        }
        
        printResultRow(status, result, position);
        
        printMatchesByDistanceTable(result);
        
        return status;
        
    }

    private int findPosition(String needleName, Collection<Fingerprint> matchesByDistance) {
        
        int position = 1;
            
        for (Fingerprint matchByDistance : matchesByDistance) {
            if(matchByDistance.getName().equals(needleName)) {
                break;
            } else {
                position++;
            }
        }
        
        return position;
    }

    private void printMatchesByDistanceTable(Result result) {
        
        Collection<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        Fingerprint needle = result.getNeedle();
        
        DETAILLOGGER.info("| {} | {} | {} | {} | {} | {} |", 
            "pos",
            "name",
            "incScore",
            "incScoreN", 
            "eucDiff",
            "eucDiffN"
        );
            
        int i = 1; 

        for(Fingerprint matchByDistance : matchesByDistance) {

            double maxLength = Math.max(matchByDistance.getLength(), needle.getLength());
            double eucDiffR  = maxLength - matchByDistance.getDistanceToFingerprint(needle);

            if(eucDiffR < 0) eucDiffR = 0;
            eucDiffR = eucDiffR  / maxLength;

            double incScoreN = 0.0d;
            if(needle.getInclusionScore() > 0.0d) {
                incScoreN = matchByDistance.getInclusionScore() / needle.getInclusionScore();
            }

            DETAILLOGGER.info("| {} | {} | {} | {} | {} | {} |", 
                    i, 
                    matchByDistance.getName(),
                    frmt.format(matchByDistance.getInclusionScore()), 
                    frmt.format(incScoreN), 
                    frmt.format(matchByDistance.getDistanceToFingerprint(needle)), 
                    frmt.format(eucDiffR)
            );
            i++;
        }
    }

    private void printResultRow(Status status, Result result, int position) {
        Fingerprint needle = result.getNeedle();
        Collection<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        String needleName  = needle.getName();
        double needleScore = needle.getInclusionScore();
        
        String firstMatchName = "<none>"; 
        double firstmatchScore = 0.0d;
        
        if(!matchesByDistance.isEmpty()) {
            Fingerprint firstMatch = matchesByDistance.iterator().next();
            firstMatchName  = firstMatch.getName();
            firstmatchScore = firstMatch.getInclusionScore();
        }
        
        double scoreN = 0.0d;
        if(needleScore > 0) {
            scoreN = firstmatchScore / needleScore;
        }
        
        RESULTLOGGER.info("| {} | {} | {} | {} | {} | {} | {} |", 
            needleName,
            frmt.format(needleScore),
            firstMatchName,
            frmt.format(firstmatchScore),
            frmt.format(scoreN), 
            (position > 0) ? position : "?",
            status
        );
    }
    
}
