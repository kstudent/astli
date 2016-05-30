package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import org.androidlibid.proto.Fingerprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.androidlibid.proto.match.FingerprintMatcher.Result;
import static org.androidlibid.proto.match.Evaluation.Classification;
import static org.androidlibid.proto.match.Evaluation.Position;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ResultEvaluator {
    
    private final NumberFormat  frmt = new DecimalFormat("#0.00");
    private static final Logger DETAILLOGGER = LogManager.getLogger(ResultEvaluator.class.getName());
    private static final Logger RESULTLOGGER = LogManager.getLogger(ResultEvaluator.class.getName() + ".Results");
    
    public Evaluation evaluateResult(Result result) {
        
        Fingerprint needle = result.getNeedle();
        Fingerprint nameMatch = result.getMatchByName();
        Collection<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        String needleName = needle.getName();
        
        Position positionStatus = Position.NO_MATCH_BY_NAME;
        
        int positionNumber = -1;
        
        if(nameMatch == null) {
            DETAILLOGGER.info("* {}: not matched by name", needleName);
        } else {
            positionNumber = findPosition(needleName, matchesByDistance);
            
            if(positionNumber > matchesByDistance.size()) {
//                DETAILLOGGER.info("* NEXT {} (max : {}) not found", 
//                        needle.getName(), 
//                        needle.getComputedSimilarityScore()
//                );
                positionStatus = Position.NOT_IN_CANDIDATES;
                positionNumber = -1;
            } else if( positionNumber > 1 ) {
                DETAILLOGGER.info("* NEXT {} (max : {}) found at position {}", 
                        needle.getName(), 
//                        frmt.format(needle.getComputedSimilarityScore()), 
                        positionNumber);
                positionStatus = Position.NOT_FIRST;
            } else {
                DETAILLOGGER.info("* {} (max : {}) found position 1", 
                        needle.getName(), 
//                        frmt.format(needle.getComputedSimilarityScore()), 
                        positionNumber);
                positionStatus = Position.OK;
            }
        }
        
        printMatchesByDistanceTable(result);
        
        Evaluation evaluation = new Evaluation();
        
        evaluation.setPosition(positionStatus);
        
        boolean matchWasInDB = (nameMatch != null);
        boolean thereAreCandidates = !matchesByDistance.isEmpty();
        
        if(positionStatus == Position.OK) {
            evaluation.setClassification(Classification.TRUE_POSITIVE);
        } else if (thereAreCandidates) {
            evaluation.setClassification(Classification.FALSE_POSITIVE);
        } else if (matchWasInDB) {
            evaluation.setClassification(Classification.FALSE_NEGATIVE);
        } else {
            evaluation.setClassification(Classification.TRUE_NEGATIVE);
        }
        printResultRow(evaluation, result, positionNumber);
        
        return evaluation;
        
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
            "simScore",
            "simScoreN", 
            "eucDiff",
            "eucDiffN"
        );
            
        int i = 1; 

        for(Fingerprint matchByDistance : matchesByDistance) {

            double maxLength = Math.max(matchByDistance.getLength(), needle.getLength());
            double eucDiffR  = maxLength - matchByDistance.getDistanceToFingerprint(needle);

            if(eucDiffR < 0) eucDiffR = 0;
            eucDiffR = eucDiffR  / maxLength;

            double simScoreN = 0.0d;
//            if(needle.getComputedSimilarityScore() > 0.0d) {
//                simScoreN = matchByDistance.getComputedSimilarityScore() / needle.getComputedSimilarityScore();
//            }

            DETAILLOGGER.info("| {} | {} | {} | {} | {} | {} |", 
                    i, 
                    matchByDistance.getName(),
//                    frmt.format(matchByDistance.getComputedSimilarityScore()), 
                    frmt.format(simScoreN), 
                    frmt.format(matchByDistance.getDistanceToFingerprint(needle)), 
                    frmt.format(eucDiffR)
            );
            i++;
        }
    }

    public void printResultRowHeader() {
        RESULTLOGGER.info("** Matches"); 
        RESULTLOGGER.info("| {} | {} | {} | {} | {} | {} | {} | {} |", 
            "apk name", 
            "apk score", 
            "lib name", 
            "lib score", 
            "sim score", 
            "#",
            "position",
            "class"
        );
    }
    
    private void printResultRow(Evaluation eval, Result result, int positionNumber) {
        Position position = eval.getPosition();
        Classification classification = eval.getClassification();
        
        Fingerprint needle = result.getNeedle();
        Collection<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        String needleName  = needle.getName();
//        double needleScore = needle.getComputedSimilarityScore();
        
        String firstMatchName = "<none>"; 
        double firstmatchScore = 0.0d;
        
        if(!matchesByDistance.isEmpty()) {
            Fingerprint firstMatch = matchesByDistance.iterator().next();
            firstMatchName  = firstMatch.getName();
//            firstmatchScore = firstMatch.getComputedSimilarityScore();
        }
        
        double scoreN = 0.0d;
//        if(needleScore > 0) {
//            scoreN = firstmatchScore / needleScore;
//        }
        
        RESULTLOGGER.info("| {} | {} | {} | {} | {} | {} | {} | {} |", 
            needleName,
//            frmt.format(needleScore),
            firstMatchName,
            frmt.format(firstmatchScore),
            frmt.format(scoreN), 
            (positionNumber > 0) ? positionNumber : "?",
            position,
            classification
        );
    }
}
