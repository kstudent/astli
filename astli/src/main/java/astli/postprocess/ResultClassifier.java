package astli.postprocess;

import astli.db.EntityService;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;
import astli.pojo.Match;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ResultClassifier {
    
    private static final NumberFormat FRMT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogManager.getLogger();
    private final EntityService service;

    public ResultClassifier(EntityService service) {
        this.service = service;
    }
    
    public ClassificationTupel classify(Match match) {
        
        List<Match.Item> items = match.getItems();
        String apkName = match.getApkH().getName();
        
        int position = IntStream.range(0, items.size())
                .filter(index -> items.get(index).getPackage().equals(apkName))
                .findFirst()
                .orElse(-1);
        
        boolean matchInList = (position != -1);
        
        double score = (position >= 0) ? items.get(position).getScore() : 0; 
        
        boolean candidatesExist = !items.isEmpty();
        
        boolean isUniqueLeader = false;
        
        if(candidatesExist) {
            double maxScore = items.get(0).getScore();
            isUniqueLeader = (items.size() == 1 || items.get(1).getScore() < maxScore);
        }
        
        boolean isOnTop = matchInList && thereIsNoBetterMatchBeforePosition(items, position, score);
        
        boolean packageInDB = false;
        try {
            packageInDB = service.isPackageNameInDB(apkName);
        } catch (SQLException ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
        
        Classification clss = isOnTop ? 
                       ( isUniqueLeader ?   Classification.TPU : Classification.TPN )
                        : candidatesExist ? Classification.FP
                        : packageInDB ?     Classification.FN 
                        :                   Classification.TN;
        
        if(clss == Classification.FP) {
            LOGGER.info("*** {} (E:{}) is a false positive", apkName, match.getApkH().getParticularity());
        } else if (clss == Classification.FN) {
            LOGGER.info("*** {} (E:{}) is a false negative", apkName, match.getApkH().getParticularity());
        }
        
        if(candidatesExist) {
            LOGGER.debug("{}' Plato : ", apkName);
            LOGGER.debug("| Name | Partic. | Score |"); 
            items.stream()
                .filter(item -> item.getScore() >= score)
                .forEach(item -> {
                    LOGGER.debug("| {} | {} | {} |", 
                        item.getPackage(), 
                        item.getParticularity(),
                        FRMT.format(item.getScore())
                    );
                });
        }
        
        return new ClassificationTupel(position, clss, score, items.size());
    }

    private boolean thereIsNoBetterMatchBeforePosition(List<Match.Item> items, 
            int position, double score) {
        return !IntStream.range(0, position)
                .boxed()
                .map(items::get)
                .anyMatch(item -> item.getScore() > score);
    }
    
    public static class ClassificationTupel {
    
        private final int position;
        private final Classification classification;
        private final double score; 
        private final int comparisons;

        public ClassificationTupel(int position, Classification classification, 
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
        TPU, //True Positive and Unique (the only label) 
        TPN, //True Positive, but also other labels with the same score
        TN, 
        FP, 
        FN;
    }
}