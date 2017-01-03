package astli.score;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;
import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class SimilarityMatcher implements PackageMatcher {

    private final HungarianAlgorithm hg = new HungarianAlgorithm();
    private List<List<Fingerprint>> bPrints;
    private List<List<Fingerprint>> aPrints;
    private List<List<String>> aSigs; 
    private List<List<String>> bSigs;
    private List<String> aClasses, bClasses;
    
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogManager.getLogger();
    
    @Override
    public synchronized double getScore(PackageHierarchy a, PackageHierarchy b) {
        
        if(a.getClassesSize() == 0 || b.getClassesSize() == 0 ) {
            return 0.0d; 
        }
        
        aPrints = a.getPrintTable();
        bPrints = b.getPrintTable();
        aSigs = a.getSignatureTable();
        bSigs = b.getSignatureTable();
        aClasses = a.getClassTable();
        bClasses = b.getClassTable();
        
        int aSize = aSigs.size(), bSize = bSigs.size();
        
        double[][] scores = new double[aSize][bSize];
        
        double max = 0;
        
        for(int i = 0; i < aSize; i++) {
            for(int j = 0; j < bSize; j++) {
                double classScore = getClassInclusionScore(i, j);
                scores[i][j] = classScore;
                if(classScore > max) max = classScore;
            }
        }
        
        double[][] scoresInverted = new double[aSize][bSize];
        invertMatrix(scores, scoresInverted, max);
        int[] solution = hg.execute(scoresInverted);
        
        double finalScore = computeScore(scores, solution);
        
        return finalScore;
    }

    private double getClassInclusionScore(int i, int j) {
        
        List<Fingerprint> printsA = aPrints.get(i);
        List<Fingerprint> printsB = bPrints.get(j);
        List<String> sigsA = aSigs.get(i);
        List<String> sigsB = bSigs.get(j);
        
        double[][] costMatrix = new double[printsA.size()][printsB.size()];
        
        double max = 0;
        
        for(int k = 0; k < printsA.size(); k++) {
            for(int l = 0; l < printsB.size(); l++) {
                if(sigsA.get(k).equals(sigsB.get(l))) {
                    double score = (double) printsA.get(k).getSimilarityTo(printsB.get(l));
                    if(score > max) {
                        max = score;
                    }
                    costMatrix[k][l] = score;
                } else { 
                    costMatrix[k][l] = 0.0d; 
                } 
            }
        }
        
        double[][] invMatrix = new double[printsA.size()][printsB.size()];
        invertMatrix(costMatrix, invMatrix, max);
        int[] solution = hg.execute(invMatrix);
        
        double score = computeScore(costMatrix, solution);
        
        return score;
        
    }

    private void invertMatrix(double[][] matrix, double[][] invertedMatrix, double offset) {
        for(int k = 0; k < matrix.length; k++) {
            for(int l = 0; l < matrix[0].length; l++) {
                invertedMatrix[k][l] = -1 * matrix[k][l] + offset; 
            }
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
        
    private void printMethodSolution(double[][] matrix, int[] solution, List<Fingerprint> printsA, List<Fingerprint> printsB) {
        
        if (!LOGGER.isInfoEnabled()) return;
        
        LOGGER.info("Scores: ");
        LOGGER.info("| M | apk | lib | score | a.E | b.E |"); 
        
        IntStream.range(0, solution.length)
            .forEach(index -> {
                if (solution[index] >= 0 && solution[index] < matrix[0].length) {
                    Fingerprint aPrint = printsA.get(index); 
                    Fingerprint bPrint = printsB.get(solution[index]);
                    LOGGER.info("| {} | {} | {} | {} | {} | {} |", 
                            aPrint.getName().equals(bPrint.getName()) ? " " : "X",
                            aPrint.getName(), 
                            bPrint.getName(),
                            FRMT.format(matrix[index][solution[index]]), 
                            aPrint.getParticularity(), 
                            bPrint.getParticularity()
                            
                    );
                }
            });
    }
    
    private void printClassSolution(double[][] matrix, int[] solution) {
        
        if (!LOGGER.isInfoEnabled()) return;

        LOGGER.info("Scores: ");
        
        IntStream.range(0, solution.length)
            .forEach(index -> {
                if (solution[index] >= 0 && solution[index] < matrix[0].length) {
                    LOGGER.info("- {} -> {}: {}", 
                            aClasses.get(index), 
                            bClasses.get(solution[index]),
                            FRMT.format(matrix[index][solution[index]])
                    );
                }
            });
    }
    
}
