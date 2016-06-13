package org.androidlibid.proto.match;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.androidlibid.proto.match.MatchingStrategy.Result;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ResultEvaluator {
    
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogManager.getLogger();
    
    public Evaluation evaluateResult(Result result) {
        
        List<MatchingStrategy.ResultItem> items = result.getItems();
        String apkName = result.getApkH().getName();
        
        int position = IntStream.range(0, items.size())
                .filter(index -> items.get(index).getPackage().equals(apkName))
                .findFirst()
                .orElse(-1);
        
        boolean isFirst         = (position == 0); 
        boolean candidatesExist = !items.isEmpty();
        boolean packageInDB     = result.isPackageInDB();
        
        Classification clss = isFirst ? Classification.TP
                    : candidatesExist ? Classification.FP
                    : packageInDB ?     Classification.FN 
                    :                   Classification.TN;
        
        double score = (position >= 0) ? items.get(position).getScore() : 0; 
        
        if(clss == Classification.FP) {
            LOGGER.info("{} (E:{}) is a false positive", apkName, result.getApkH().getEntropy());
        } else if (clss == Classification.FN) {
            LOGGER.info("{} (E:{}) is a false negative", apkName, result.getApkH().getEntropy());
        }
        
        IntStream.range(0, position).forEach(index -> 
            LOGGER.info("- {}. : {} ({})", 
                index,
                items.get(index).getPackage(), 
                FRMT.format(items.get(index).getScore()))
        );

        return new Evaluation(position, clss, score, items.size());
    }
    
    public static class Evaluation {
    
        private final int position;
        private final Classification classification;
        private final double score; 
        private final int comparisons;

        public Evaluation(int position, Classification classification, 
                double score, int comparisons) {
            this.position = position;
            this.classification = classification;
            this.score = score; 
            this.comparisons = comparisons;
        }

        public int getPosition() {
            return position;
        }

        public Classification getClassification() {
            return classification;
        }

        public double getScore() {
            return score;
        }

        public int getComparisons() {
            return comparisons;
        }
    }
    
    public static enum Classification {
        TP, TN, FP, FN;
    }
}