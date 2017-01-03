package astli.postprocess;

import astli.db.EntityService;
import astli.pojo.ASTLIOptions;
import java.util.Date;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PostProcessorFactory {

    public static PostProcessor createProcessor(ASTLIOptions options, EntityService service) {

        if(options.isInEvaluationMode) {
            return new EvaluateResults(options, new ResultClassifier(service), new Date());
        }
        
        return new PlateauFilterProcessor(new PrintResultTable());
        
    }
    
}
