package org.androidlibid.proto.match.postprocess;

import java.util.HashMap;
import java.util.Map;
import org.androidlibid.proto.pojo.ASTLIOptions;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StatsCounter {

    private final Map<ResultClassifier.Classification, Integer> classifications;
    private final Map<Integer, Integer> positions;
    private int comparisonCounter = 0;
    
    public StatsCounter() {
        classifications = new HashMap<>();
        for(ResultClassifier.Classification key : ResultClassifier.Classification.values()) {
            classifications.put(key, 0);
        }
        positions = new HashMap<>();
    }
    
    synchronized public void incrementStats(ResultClassifier.ClassificationTupel evaluation) {
        
        int position = evaluation.getPosition();
        Integer posCounter = positions.get(position);
        if(posCounter == null) {
            posCounter = 0;
        }
        positions.put(position, posCounter + 1);
        classifications.put(evaluation.getClassification(), classifications.get(evaluation.getClassification()) + 1);
        comparisonCounter += evaluation.getComparisons();
    } 
    
    public Stats getStats() {
        return new Stats(classifications, positions, comparisonCounter);
    }
    
    public static class Stats {
        private final Map<ResultClassifier.Classification, Integer> classifications;
        private final Map<Integer, Integer> positions;
        private final int comparisonCounter;
        
        private String obfLvl = "";
        private String apkName = "";
        private String algorithm = "";
        private long diff;
        
        public Map<ResultClassifier.Classification, Integer> getClassifications() {
            return classifications;
        }

        public Map<Integer, Integer> getPositions() {
            return positions;
        }

        public int getComparisonCounter() {
            return comparisonCounter;
        }

        public String getObfLvl() {
            return obfLvl;
        }

        public String getApkName() {
            return apkName;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public long getDiff() {
            return diff;
        }
        
        public void setOptions(ASTLIOptions options) {
            algorithm = options.process.getSimpleName(); 
            String[] pieces = options.inputFileName.split("/");
            obfLvl    = pieces[pieces.length - 1];
            apkName   = (pieces.length > 1) ? pieces[pieces.length - 2] : "<unknown>";
        }
        
        void setDiff(long diff) {
            this.diff = diff;
        }
        
        public Stats(Map<ResultClassifier.Classification, Integer> classifications, 
                Map<Integer, Integer> positions, int comparisonCounter) {
            this.classifications = classifications;
            this.positions = positions;
            this.comparisonCounter = comparisonCounter;
        }

    }

}
