package astli.postprocess;

import astli.db.EntityService;
import astli.pojo.Match;
import astli.pojo.PackageHierarchy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchToCSVLogger implements PostProcessor {

    private static final Logger LOG = LogManager.getLogger();
    private static final String NEGATIVE = "<negative>"; 
    
    private final EntityService service;

    public MatchToCSVLogger(EntityService service) {
        this.service = service;
    }
    
    @Override
    public void init() {
    }

    @Override
    public void process(Match match) {
        
        PackageHierarchy apkH = match.getApkH();

        String actualName;
        try {
            actualName = service.isPackageNameInDB(apkH.getName()) 
                    ? apkH.getName()
                    : NEGATIVE;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        List<String> predicted = new ArrayList<>();
        
        double maxScore = 0.0d;
        
        if(match.getItems().isEmpty()) {
            predicted.add(NEGATIVE);
        } else {
            maxScore = match.getItems().get(0).getScore();
            
            for(Match.Item item : match.getItems()) {
                if(item.getScore() < maxScore) break; 
                predicted.add(item.getPackage());
            }
        } 
        
        final double score = maxScore;
        
        predicted.stream()
                .forEach(predictedName -> LOG.info("{},{},{},{},{}", 
                        apkH.getName(), apkH.getParticularity(), actualName, 
                        predictedName, score));
    }

    @Override
    public void done() {
    }
    
}
