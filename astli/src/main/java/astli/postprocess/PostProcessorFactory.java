package astli.postprocess;

import astli.pojo.ASTLIOptions;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PostProcessorFactory {

    public PostProcessor createProcessor(ASTLIOptions options) {
        
        //todo... something like if production else evaluation
        // return new EvaluationModeProcessor(options, new Date());
        return new PlateauFilterProcessor(new PrintResultTable());

    }
    
}
