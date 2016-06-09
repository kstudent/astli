package org.androidlibid.proto.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.androidlibid.proto.PackageHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageSignatureMatcher {
    
    private List<List<String>> a, b;
    private double[][] cost;
    private final HungarianAlgorithm hg;
    
    private static final Logger LOGGER = LogManager.getLogger(PackageSignatureMatcher.class);

    public PackageSignatureMatcher(HungarianAlgorithm hg) {
        this.hg = hg;
    }
    
    /**
     * Checks if a's signatures are in b
     * @param a
     * @param b
     * @return 
     */
    public synchronized Result checkSignatureInclusion(PackageHierarchy a, PackageHierarchy b) {
        
        if(a.getClassesSize() > b.getClassesSize() || a.getClassesSize() == 0) {
            return new Result(false); 
        }
        
        this.a = a.getSignatureTable();
        this.b = b.getSignatureTable();
        
        if(this.a.size() > this.b.size() || this.a.isEmpty()) {
            return new Result(false);
        } 
        
        this.cost = new double[this.a.size()][this.b.size()];
        
        int[] solution = new int[this.a.size()];
        Arrays.fill(solution, -1);

        boolean matchingIsPossible = initCosts();
        
        if(matchingIsPossible) {
            solution = hg.execute(cost);
        }
        
        printMatrix(solution);
        
        return isSolutionFeasible(solution);
    }

    private boolean initCosts() {
        for(int i = 0; i < a.size(); i++) {
            for(int j = 0; j < b.size(); j++) {
                cost[i][j] = Double.POSITIVE_INFINITY;
            }
        }
        
        for(int i = 0; i < a.size(); i++) {
            
            boolean matchForAExists = false; 
            
            for(int j = 0; j < b.size(); j++) {
                boolean matched = checkClassInclusion(i, j);
                matchForAExists = matched || matchForAExists;
                cost[i][j] = (matched ? 0 : 1); 
            }
            
            if (!matchForAExists) return false;
        }
        
        return true;
    }

    private boolean checkClassInclusion(int i, int j) {
        List<String> methodsA = a.get(i);
        List<String> methodsB = new ArrayList(b.get(j));
        
        if(methodsA.size() > methodsB.size()) {
            return false;
        }
        
        for(String methodA : methodsA) {
            if(!methodsB.remove(methodA)) {
                return false;
            }
        }
        
        return true;
    }

    private void printMatrix(int[] solution) {
        
        if (!LOGGER.isInfoEnabled()) return;
        
        for(int i = 0; i < cost.length; i++) {

            StringBuilder row = new StringBuilder("|");

            for(int j = 0; j < cost[0].length; j++) {
                boolean selected = solution[i] == j; 
                char state = (cost[i][j] == 0.0d) 
                        ? (selected ? 'X' : '-') 
                        : (selected ? '~' : ' '); 
                row.append(state).append(" |");
            }

            LOGGER.info(row);
        }
    }
    
    private Result isSolutionFeasible(int[] solution) {
        for(int j = 0; j < solution.length; j++) {
            if (solution[j] < 0 || cost[j][solution[j]] > 0) 
                return new Result(false);  
        }
        return new Result(true, cost);
    }

    public static class Result {
        
        private final boolean packagesMatch; 
        private final double[][] costMatrix;

        public Result(boolean packagesMatch, double[][] costMatrix) {
            this.packagesMatch = packagesMatch;
            this.costMatrix = costMatrix;
        }

        public Result(boolean packagesMatch) {
            this(packagesMatch, new double[0][0]);
        }

        public boolean packageAIsIncludedInB() {
            return packagesMatch;
        }

        public double[][] getCostMatrix() {
            return costMatrix;
        }
    }
}
