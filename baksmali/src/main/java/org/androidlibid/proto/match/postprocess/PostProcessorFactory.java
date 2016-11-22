package org.androidlibid.proto.match.postprocess;

import org.androidlibid.proto.ASTLIOptions;

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
