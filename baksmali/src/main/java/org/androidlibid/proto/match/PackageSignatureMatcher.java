package org.androidlibid.proto.match;

import java.util.ArrayList;
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
    private byte[][] inc;
    
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
        this.inc = new byte[this.a.size()][this.b.size()];
        
        boolean matchingIsPossible = initInclusionMatrix();
        
        printMatrix();
        
            return matchingIsPossible;
    }

    private boolean initInclusionMatrix() {
        for(int i = 0; i < a.size(); i++) {
            for(int j = 0; j < b.size(); j++) {
                inc[i][j] = Byte.MAX_VALUE;
            }
        }
        
        for(int i = 0; i < a.size(); i++) {
            
            boolean matchForAExists = false; 
            
            for(int j = 0; j < b.size(); j++) {
                boolean matched = checkInclusion(i, j);
                matchForAExists = matched || matchForAExists;
                inc[i][j] = (byte) (matched ? 0 : 1); 
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

    private void printMatrix() {
        for(int i = 0; i < a.size(); i++) {
            
            StringBuilder row = new StringBuilder("|");
            
            for(int j = 0; j < b.size(); j++) {
                
                char state = (inc[i][j] == 0) ? 'X' : (inc[i][j] == 1) ? ' ' : '?'; 
                row.append(state).append(" |");
            }
            
            LOGGER.info(row);
        }
    }

    
}
