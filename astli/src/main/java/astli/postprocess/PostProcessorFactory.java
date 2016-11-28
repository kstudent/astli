package astli.postprocess;

import astli.db.EntityService;
import astli.pojo.ASTLIOptions;
import java.util.Date;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PostProcessorFactory {

    public PostProcessor createPrintResultsProcessor() {
        return new PlateauFilterProcessor(new PrintResultTable());
    }
    
    public PostProcessor createEvaluateResultsProcessor(ASTLIOptions options, EntityService service) {
        return new EvaluateResults(options, new ResultClassifier(service), new Date());
    }
    
}
