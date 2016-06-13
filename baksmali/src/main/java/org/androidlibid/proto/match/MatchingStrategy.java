package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.PackageHierarchy;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public abstract class MatchingStrategy {

    private final Map<ResultEvaluator.Classification, Integer> classifications;
    private final Map<Integer, Integer> positions;
    private int comparisonCounter = 0;
    private int dbLookups = 0;
    
    public MatchingStrategy() {
        classifications = new HashMap<>();
        for(ResultEvaluator.Classification key : ResultEvaluator.Classification.values()) {
            classifications.put(key, 0);
        }
        positions = new HashMap<>();
    }
    
    synchronized protected void incrementStats(ResultEvaluator.Evaluation evaluation) {
        
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
        return new Stats(classifications, positions, comparisonCounter, dbLookups);
    }
    
    protected void incDbLookup(){
        dbLookups++;
    }
    
    abstract public void matchHierarchies(Map<String, PackageHierarchy> hierarchies) 
            throws SQLException;
    
    public static class ResultItem {
        
        private final double score; 
        private final String packageName;

        public ResultItem(double score, String packageName) {
            this.score = score;
            this.packageName = packageName;
        }
        
        public String getPackage() {
            return packageName;
        }

        public double getScore() {
            return score;
        }
    }
    
    public static class Result {
        
        private final List<ResultItem> items; 
        private final PackageHierarchy apkH;
        private final boolean packageInDB; 

        public Result(List<ResultItem> items, PackageHierarchy apkH, boolean packageInDB) {
            this.items = items;
            this.apkH = apkH;
            this.packageInDB = packageInDB;
        }

        public boolean isPackageInDB() {
            return packageInDB;
        }

        public List<ResultItem> getItems() {
            return items;
        }

        public PackageHierarchy getApkH() {
            return apkH;
        }
    }
    
    public static class Stats {
        private final Map<ResultEvaluator.Classification, Integer> classifications;
        private final Map<Integer, Integer> positions;
        private final int comparisonCounter;
        private final int dbLookups;

        
        public Map<ResultEvaluator.Classification, Integer> getClassifications() {
            return classifications;
        }

        public Map<Integer, Integer> getPositions() {
            return positions;
        }

        public int getComparisonCounter() {
            return comparisonCounter;
        }

        public int getDbLookups() {
            return dbLookups;
        }
        
        public Stats(Map<ResultEvaluator.Classification, Integer> classifications, 
                Map<Integer, Integer> positions, int comparisonCounter,
                int dbLookups) {
            this.classifications = classifications;
            this.positions = positions;
            this.comparisonCounter = comparisonCounter;
            this.dbLookups = dbLookups;
        }
    }

}
