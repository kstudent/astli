package org.androidlibid.proto.match;

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
public class MatchFingerprintsAlgorithm implements AndroidLibIDAlgorithm {

    private final baksmaliOptions options;
    private final List<? extends ClassDef> classDefs;
    
    private static final Logger LOGGER = LogManager.getLogger( MatchFingerprintsAlgorithm.class.getName() );
    private final ASTBuilderFactory astBuilderFactory;
       
    public MatchFingerprintsAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options   = options;
        this.classDefs = classDefs;
        astBuilderFactory = new ASTBuilderFactory(options);
        
    }
    
    @Override
    public boolean run() {
        try {
            Date before = new Date();
            MatchingStrategy strategy = setupStrategy(); 
            Stream<PackageHierarchy> hierarchies = generatePackagePrintStream();
            strategy.matchHierarchies(hierarchies);
            
            Date after = new Date();
            long diff = after.getTime() - before.getTime();
            
            
            MatchingStrategy.Stats stats = strategy.getStats();
            
            stats.setDiff(diff);
            stats.setOptions(options);
            
            new StatsToJsonLogger().logStats(stats);
            new StatsToOrgLogger().logStats(stats);
            
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

    private MatchingStrategy setupStrategy() throws SQLException {
        
        EntityService service = EntityServiceFactory.createService();
        FingerprintService fpService = new FingerprintService(service);
        ResultEvaluator evaluator = new ResultEvaluator();
        
        new SetupLogger(options, service).logSetup();
        
        if(options.strategy.equals(HybridStrategy.class)) {
            return new HybridStrategy(fpService, 12, evaluator);
//            return new HybridAlternativeStrategy(fpService, 12, evaluator);
        }
        
        return new ConfusionMatrixStrategy(fpService);
        
    }
}
