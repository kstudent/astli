package org.androidlibid.proto.match;

import org.androidlibid.proto.match.postprocess.EvaluateResults;
import org.androidlibid.proto.match.postprocess.PostProcessor;
import org.androidlibid.proto.match.matcher.SimilarityMatcher;
import org.androidlibid.proto.match.matcher.HungarianAlgorithm;
import org.androidlibid.proto.match.finder.ParticularCandidateFinder;
import org.androidlibid.proto.match.finder.CandidateFinder;
import org.androidlibid.proto.AndroidLibIDAlgorithm;
import org.androidlibid.proto.ao.FingerprintService;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.PackageHierarchyGenerator;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ast.ASTBuilderFactory;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchAlgorithm implements AndroidLibIDAlgorithm {

    private final baksmaliOptions options;
    private final List<? extends ClassDef> classDefs;
    
    private static final Logger LOGGER = LogManager.getLogger();
    private final ASTBuilderFactory astBuilderFactory;
       
    public MatchAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options   = options;
        this.classDefs = classDefs;
        astBuilderFactory = new ASTBuilderFactory(options);
        
    }
    
    @Override
    public boolean run() {
        try {
            MatchingProcess process = setupProcess(); 
            PostProcessor processor = setupPostProcessor();
            generatePackagePrintStream()
                    .map(hierarchy  -> process.apply(hierarchy))
                    .forEach(result -> processor.process(result));
            
            processor.done();
            
        } catch (SQLException | IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return true;
    }

    private Stream<PackageHierarchy> generatePackagePrintStream() throws IOException {
        
        LOGGER.info("** Create Package Prints");
        
        Map<String, String> mappings = new HashMap<>();

        if(options.isObfuscated) {
            
            BufferedReader classReader  = new BufferedReader(new FileReader(options.mappingFile));
            BufferedReader methodReader = new BufferedReader(new FileReader(options.mappingFile));
            
            ProGuardMappingFileParser parser = new ProGuardMappingFileParser(); 
            mappings = parser.parseMappingFileOnMethodLevel(classReader, methodReader);
        } 

        PackageHierarchyGenerator phGen = new PackageHierarchyGenerator(
                options, new ASTToFingerprintTransformer(), mappings);
        
        Stream<ASTClassBuilder> builderStream = classDefs.parallelStream()
                .map(classDef -> new ASTClassBuilder(classDef, astBuilderFactory));
        
        return phGen.generatePackageHierarchiesFromClassBuilders(builderStream);
        
    }

    private MatchingProcess setupProcess() throws SQLException {
        
        EntityService service = EntityServiceFactory.createService();
        FingerprintService fpService = new FingerprintService(service);
        CandidateFinder finder = new ParticularCandidateFinder(fpService);
        SimilarityMatcher matcher = new SimilarityMatcher(new HungarianAlgorithm());
        
        new SetupLogger(options, service).logSetup();
        
        return new MatchingProcess(fpService, matcher, finder);
    }

    private PostProcessor setupPostProcessor() {
        return new EvaluateResults(options, new Date());
    }
}
