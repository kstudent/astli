package astli.match;

import astli.postprocess.PostProcessorFactory;
import astli.score.SimilarityMatcher;
import astli.score.HungarianAlgorithm;
import astli.find.ParticularCandidateFinder;
import astli.find.CandidateFinder;
import astli.main.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.stream.Stream;
import astli.pojo.ASTLIOptions;
import astli.pojo.PackageHierarchy;
import astli.db.EntityService;
import astli.db.EntityServiceFactory;
import astli.postprocess.PostProcessor;
import astli.pojo.Match;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchAlgorithm implements AndroidLibIDAlgorithm {

    private final Stream<PackageHierarchy> packages;
    
    private static final Logger LOGGER = LogManager.getLogger();
    private final ASTLIOptions options;

    public MatchAlgorithm(Stream<PackageHierarchy> packages, ASTLIOptions astliOptions) {
        this.packages = packages;
        this.options = astliOptions;
    }
    
    @Override
    public void run() {
        try {
            EntityService service = EntityServiceFactory.createService();
            CandidateFinder finder = new ParticularCandidateFinder(service);
            SimilarityMatcher matcher = new SimilarityMatcher(new HungarianAlgorithm());
            PostProcessor processor = new PostProcessorFactory().createPrintResultsProcessor();
            MatchingProcess process = new MatchingProcess(matcher, finder);
            
            new SetupLogger(service, options).logSetup();
            
            processor.init();

            packages.map(hierarchy  -> process.apply(hierarchy))
                    .forEach((Match match) -> processor.process(match));

            processor.done();
            
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
