package org.androidlibid.proto.match;

import org.androidlibid.proto.match.postprocess.PostProcessorFactory;
import org.androidlibid.proto.match.matcher.SimilarityMatcher;
import org.androidlibid.proto.match.matcher.HungarianAlgorithm;
import org.androidlibid.proto.match.finder.ParticularCandidateFinder;
import org.androidlibid.proto.match.finder.CandidateFinder;
import org.androidlibid.proto.AndroidLibIDAlgorithm;
import org.androidlibid.proto.ao.FingerprintService;
import java.sql.SQLException;
import java.util.stream.Stream;
import org.androidlibid.proto.ASTLIOptions;
import org.androidlibid.proto.pojo.PackageHierarchy;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.match.postprocess.PostProcessor;
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
                    .forEach(result -> processor.process(result));
            
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
