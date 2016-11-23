package org.androidlibid.proto.match.postprocess;

import java.util.Date;
import org.androidlibid.proto.ASTLIOptions;
import org.androidlibid.proto.pojo.Match;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class EvaluateResults implements PostProcessor {

    private final ResultClassifier classifier;
    private final StatsCounter counter;
    
    private final ASTLIOptions options;
    private final Date before;
    
    private static final Logger LOGGER = LogManager.getLogger();

    public EvaluateResults(ASTLIOptions options, Date before) {
        this.classifier = new ResultClassifier();
        this.counter = new StatsCounter();
        this.options = options;
        this.before = before;
    }
    
    @Override
    public void init() {
    }

    @Override
    public void process(Match match) {
        
        ResultClassifier.ClassificationTupel classification = classifier.classify(match);
        
        String libPckg = (match.getItems().size()>0) ? match.getItems().get(0).getLib() : "";
        LOGGER.info("{},{},{},{},{}", 
            classification.getScore(), 
            classification.getClassification(),
            match.getApkH().getEntropy(),
            match.getApkH().getName(),
            libPckg
        );
        
        counter.incrementStats(classification);
    }
    
    @Override
    public void done() {
        StatsCounter.Stats stats = counter.getStats();
        stats.setOptions(options);
        long diff = new Date().getTime() - before.getTime();            
        
        stats.setDiff(diff);
        new StatsToJsonLogger().accept(stats);
        new StatsToOrgLogger().accept(stats);
    }

    
}
