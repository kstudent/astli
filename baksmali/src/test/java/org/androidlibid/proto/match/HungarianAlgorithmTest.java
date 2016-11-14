package org.androidlibid.proto.match;

import org.androidlibid.proto.match.matcher.HungarianAlgorithm;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class HungarianAlgorithmTest {
    
    double i = 1;
    
    @Test
    public void testHungarianAlgorithmSimple() {
        
         
        
        double[][] costMatrix = new double[][]{
            {0, i, i, i, i},
            {i, 0, i, i, i},
            {i, i, 0, i, i},
            {i, i, i, 0, i},
            {i, i, i, i, 0}
        };
        
        HungarianAlgorithm ha = new HungarianAlgorithm();
        
        int[] solution = ha.execute(costMatrix);
        
        printSolution(costMatrix, solution);
    }
    
     @Test
    public void testImpossibleSoltion1() {
        
        double[][] costMatrix = new double[][]{
            {0, i, i, i, i},
            {i, 0, i, 0, i},
            {i, i, 0, i, i},
            {i, i, i, i, 0},
            {i, i, i, i, 0}
        };
        
        HungarianAlgorithm ha = new HungarianAlgorithm();

        int[] solution = ha.execute(costMatrix);
        
        printSolution(costMatrix, solution);
        
    }
    
     @Test
    public void testImpossibleSoltion2() {
        
        double[][] costMatrix = new double[][]{
            {0, 0, i, i, 0},
            {i, 0, 0, 0, 0},
            {i, i, 0, i, i},
            {i, i, i, i, 0},
            {i, i, 0, i, i}
        };
        
        HungarianAlgorithm ha = new HungarianAlgorithm();

        int[] solution = ha.execute(costMatrix);
        
        printSolution(costMatrix, solution);
        
    }
    
    @Test
    public void testAssymmetric() {
        
        double[][] costMatrix = new double[][]{
            {0, i, i, i, i, i, i, i, i},
            {i, 0, i, i, i, i, i, i, i},
            {i, i, 0, i, i, i, i, i, i},
            {i, i, i, 0, i, i, i, i, i},
            {i, i, i, i, 0, i, i, i, i}
        };
        
        HungarianAlgorithm ha = new HungarianAlgorithm();

        int[] solution = ha.execute(costMatrix);
        
        printSolution(costMatrix, solution);
        
    }
    
    

    private void printSolution(double[][] inc, int[] solution) {
        
        for(int i = 0; i < inc.length; i++) {

            StringBuilder row = new StringBuilder(i + 1 + ": |");

            for(int j = 0; j < inc[0].length; j++) {
                boolean selected = solution[i] == j; 
                char state = (inc[i][j] == 0) ? (selected ? 'X' : '-') : (selected ? '~' : ' '); 
                row.append(state).append(" |");
            }

            System.out.println(row);
        }
        
        double cost = 0; 
        for(int j = 0; j < solution.length; j++) {
            
            cost += inc[j][solution[j]]; 
            
            System.out.println("Worker " + (j+1) + " does job " + (solution[j] + 1));
        }
        
        System.out.println("Cost: " + cost);
    }
    
}
