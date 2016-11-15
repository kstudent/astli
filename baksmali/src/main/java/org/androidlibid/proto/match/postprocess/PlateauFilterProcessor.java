package org.androidlibid.proto.match.postprocess;

import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.androidlibid.proto.match.MatchingProcess;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PlateauFilterProcessor implements PostProcessor {

    private final PostProcessor actualProcessor;

    public PlateauFilterProcessor(PostProcessor actualProcessor) {
        this.actualProcessor = actualProcessor;
    }
    
    @Override
    public void init() {
        actualProcessor.init();
    }
    
    @Override
    public void process(MatchingProcess.Result result) {

        double max = result.getItems().stream()
                .mapToDouble(item -> item.getScore())
                .max()
                .orElse(0);
        
        List<MatchingProcess.ResultItem> plateau;
        
        if(max > 0) {
            plateau = result.getItems().stream()
                    .filter(item -> item.getScore() == max)
                    .collect(toList());
        } else {
           plateau = new ArrayList<>();
        }
        
        MatchingProcess.Result filteredResult = new MatchingProcess.Result(plateau, result.getApkH(), result.isPackageInDB());
        
        actualProcessor.process(filteredResult);
    }

    @Override
    public void done() {
        actualProcessor.done();
    }
}
