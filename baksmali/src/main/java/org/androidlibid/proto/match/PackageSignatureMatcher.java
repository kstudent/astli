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
    
    private static final Logger LOGGER = LogManager.getLogger(PackageSignatureMatcher.class);
    
    /**
     * Checks if a's signatures are in b
     * @param a
     * @param b
     * @return 
     */
    public boolean checkSignatureInclusion(PackageHierarchy a, PackageHierarchy b) {
        
        this.a = a.getSignatureTable();
        this.b = b.getSignatureTable();
        this.cost = new double[this.a.size()][this.b.size()];
        
        int[] solution = new int[this.a.size()];
        Arrays.fill(solution, -1);

        boolean matchingIsPossible = initCosts();
        
        if(matchingIsPossible) {
            HungarianAlgorithm hg = new HungarianAlgorithm(cost);
            solution = hg.execute();
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
                boolean matched = checkInclusion(i, j);
                matchForAExists = matched || matchForAExists;
                cost[i][j] = (matched ? 0 : 1); 
            }
            
            if (!matchForAExists) return false;
        }
        
        return true;
    }

    private boolean checkInclusion(int i, int j) {
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
                char state = (cost[i][j] == 0.0d) ? (selected ? 'X' : '-') : (selected ? '~' : ' '); 
                row.append(state).append(" |");
            }

            LOGGER.info(row);
        }
    }
    
    private boolean isSolutionFeasible(int[] solution) {
        for(int j = 0; j < solution.length; j++) {
            if (solution[j] < 0 || cost[j][solution[j]] > 0) 
                return false;  
        }
        return true;
    }

    
}
