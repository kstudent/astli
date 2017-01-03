package astli.match;

import astli.postprocess.PostProcessorFactory;
import astli.find.CandidateFinder;
import astli.main.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.stream.Stream;
import astli.pojo.ASTLIOptions;
import astli.pojo.PackageHierarchy;
import astli.db.EntityService;
import astli.db.EntityServiceFactory;
import astli.find.FinderFactory;
import astli.postprocess.PostProcessor;
import astli.pojo.Match;
import astli.score.MatcherFactory;
import astli.score.PackageMatcher;
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
            
            CandidateFinder finder = FinderFactory.createFinder(options, service); 
            PackageMatcher matcher = MatcherFactory.createMatcher(options.matcher); 
            MatchingProcess matchingProcess = new MatchingProcess(
                    matcher, finder, options.packageAcceptanceThreshold);
            PostProcessor post = PostProcessorFactory.createProcessor(options, service);
            
            new SetupLogger(service, options).logSetup();
            
            post.init();

            final int minPart = options.minimumPackageParticularity; 
            
            packages
                .filter(hierarchy -> hierarchy.getParticularity() >= minPart)
                .map(hierarchy -> matchingProcess.apply(hierarchy))
                .forEach((Match match) -> post.process(match));

            post.done();
            
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
