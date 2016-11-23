package astli.match;

import astli.postprocess.PostProcessorFactory;
import astli.score.SimilarityMatcher;
import astli.score.HungarianAlgorithm;
import astli.find.ParticularCandidateFinder;
import astli.find.CandidateFinder;
import astli.main.AndroidLibIDAlgorithm;
import astli.db.FingerprintService;
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

    private final ASTLIOptions astliOptions;
    private final Stream<PackageHierarchy> packages;
    
    private static final Logger LOGGER = LogManager.getLogger();
    private final PostProcessor processor;

    public MatchAlgorithm(Stream<PackageHierarchy> packages, ASTLIOptions astliOptions) {
        this.astliOptions = astliOptions;
        this.packages = packages;
        this.processor = new PostProcessorFactory().createProcessor(astliOptions);
    }
    
    @Override
    public void run() {
        try {
            MatchingProcess process = setupProcess(); 
            
            processor.init();
            
            packages.map(hierarchy  -> process.apply(hierarchy))
                    .forEach((Match match) -> processor.process(match));
            
            processor.done();
            
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private MatchingProcess setupProcess() throws SQLException {
        
        EntityService service = EntityServiceFactory.createService();
        FingerprintService fpService = new FingerprintService(service);
        CandidateFinder finder = new ParticularCandidateFinder(fpService);
        SimilarityMatcher matcher = new SimilarityMatcher(new HungarianAlgorithm());
        
        new SetupLogger(service, astliOptions).logSetup();
        
        return new MatchingProcess(fpService, matcher, finder);
    }
}
