package astli.postprocess;

import java.util.ArrayList;
import java.util.List;
import astli.pojo.Match;
import static java.util.stream.Collectors.toList;

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
    public void process(Match result) {

        double max = result.getItems().stream()
                .mapToDouble(item -> item.getScore())
                .max()
                .orElse(0);
        
        List<Match.Item> plateau;
        
        if(max > 0) {
            plateau = result.getItems().stream()
                    .filter(item -> item.getScore() == max)
                    .collect(toList());
        } else {
           plateau = new ArrayList<>();
        }
        
        Match filteredResult = new Match(plateau, result.getApkH());
        
        actualProcessor.process(filteredResult);
    }

    @Override
    public void done(int totalPackages, int keptPackages) {
        actualProcessor.done(totalPackages, keptPackages);
    }
}
