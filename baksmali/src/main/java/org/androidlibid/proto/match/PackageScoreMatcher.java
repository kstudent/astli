package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class PackageScoreMatcher {

    private final HungarianAlgorithm hg;
    private List<List<Fingerprint>> bPrints;
    private List<List<Fingerprint>> aPrints;
    private List<List<String>> aSigs; //TODO
    private List<List<String>> bSigs; //TODO
    
    private static final double SIGNATURES_MATCH = 0.0d;
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogManager.getLogger();
    
    public PackageScoreMatcher(HungarianAlgorithm hg) {
        this.hg = hg;
    }
    
    public double getScore(PackageHierarchy a, PackageHierarchy b, double[][] sigM) {
        
        if(a.getClassesSize() == 0 || b.getClassesSize() == 0 ) {
            return 0.0d; 
        }
        
        double[][] scores  = new double[sigM.length][sigM[0].length];
        
        aPrints = a.getPrintTable();
        bPrints = b.getPrintTable();
        aSigs = a.getSignatureTable(); //TODO
        bSigs = b.getSignatureTable(); //TODO
        
        double max = 0;
        
        for(int i = 0; i < sigM.length; i++) {
            for(int j = 0; j < sigM[0].length; j++) {
                if(sigM[i][j] == SIGNATURES_MATCH) {
                    double classScore = getClassInclusionScore(i, j);
                    scores[i][j] = classScore;
                    if(classScore > max) max = classScore;
                } else {
                    scores[i][j] = 0.0d;
                }
            }
        }
        
        double[][] scoresInverted = new double[sigM.length][sigM[0].length];
        invertMatrix(scores, scoresInverted, max);
        int[] solution = hg.execute(scoresInverted);
        
        double finalScore = computeScore(scores, solution);
        
        LOGGER.info("** matrix of {} -> {} (score: {})", a.getName(), b.getName(), FRMT.format(finalScore));
        printMatrix(scores, solution);
        
        return finalScore;
    }

    private double getClassInclusionScore(int i, int j) {
        
        List<Fingerprint> printsA = aPrints.get(i);
        List<Fingerprint> printsB = bPrints.get(j);
        List<String> sigsA = aSigs.get(i); //TODO
        List<String> sigsB = bSigs.get(j); //TODO
        
        double[][] costMatrix = new double[printsA.size()][printsB.size()];
        
        double max = 0;
        
        for(int k = 0; k < printsA.size(); k++) {
            for(int l = 0; l < printsB.size(); l++) {
                if(sigsA.get(k).equals(sigsB.get(l))) { //TODO
                    double score = printsA.get(k).getNonCommutativeSimilarityScoreToFingerprint(printsB.get(l));
                    if(score > max) {
                        max = score;
                    }
                    costMatrix[k][l] = score;
                } else { //TODO
                    costMatrix[k][l] = 0.0d; //TODO
                } //TODO
            }
        }
        
        double[][] invMatrix = new double[printsA.size()][printsB.size()];
        invertMatrix(costMatrix, invMatrix, max);
        int[] solution = hg.execute(invMatrix);
        
        return computeScore(costMatrix, solution);
        
    }

    private void invertMatrix(double[][] matrix, double[][] invertedMatrix, double offset) {
        for(int k = 0; k < matrix.length; k++) {
            for(int l = 0; l < matrix[0].length; l++) {
                invertedMatrix[k][l] = -1 * matrix[k][l] + offset; 
            }
        }
    }
    
    private void printMatrix(double[][] matrix, int[] solution) {
        
        if (!LOGGER.isInfoEnabled()) return;
        
        for(int i = 0; i < matrix.length; i++) {

            StringBuilder row = new StringBuilder("|");

            for(int j = 0; j < matrix[0].length; j++) {
                boolean selected = solution[i] == j; 
                String state = (matrix[i][j] == 0.0d) 
                        ? (selected ? "*" : " ") 
                        : (selected ? "* " + FRMT.format(matrix[i][j]) : FRMT.format(matrix[i][j])); 
                row.append(state).append(" | ");
            }

            LOGGER.info(row);
        }
    }
    
    private double computeScore(double[][] matrix, int[] solution) {
        return IntStream.range(0, solution.length)
            .mapToDouble(index -> 
                (solution[index] >= 0 && solution[index] < matrix[0].length) 
                    ? matrix[index][solution[index]] 
                    : 0.0d
            )
            .sum();
    }
    
}
