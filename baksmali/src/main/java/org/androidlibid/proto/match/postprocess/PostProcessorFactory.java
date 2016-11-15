package org.androidlibid.proto.match.postprocess;

import java.util.Date;
import org.jf.baksmali.baksmaliOptions;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PostProcessorFactory {

    public PostProcessor createProcessor(baksmaliOptions options) {
        
        //todo... something like if production else evaluation
        // return new EvaluationModeProcessor(options, new Date());
        return new PlateauFilterProcessor(new PrintResultTable());

    }
    
}
