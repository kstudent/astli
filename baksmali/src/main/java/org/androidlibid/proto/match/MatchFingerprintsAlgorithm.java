package org.androidlibid.proto.match;

import org.androidlibid.proto.match.vector.VectorDifferenceStrategy;
import org.androidlibid.proto.match.inclusion.ClassInclusionCalculator;
import org.androidlibid.proto.match.inclusion.PackageInclusionCalculator;
import org.androidlibid.proto.match.inclusion.InclusionStrategy;
import org.androidlibid.proto.AndroidLibIDAlgorithm;
import org.androidlibid.proto.ao.FingerprintService;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.PackageHierarchyGenerator;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ast.ASTBuilderFactory;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.match.Evaluation.Classification;
import org.androidlibid.proto.match.Evaluation.Position;
import org.androidlibid.proto.match.inclusion.QuickInclusionCalculator;
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
       
    private Set<String> interestedPackageNames = new HashSet<>();
    
    public MatchFingerprintsAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options   = options;
        this.classDefs = classDefs;
        astBuilderFactory = new ASTBuilderFactory(options);
        
//        interestedPackageNames.add("org.spongycastle.util.encoders");
        
    }
    
    @Override
    public boolean run() {
//        try {
//            
//            Date before = new Date();
//            
//            MatchingStrategy strategy = setupStrategy(); 
//            
//            Map<String, Fingerprint> packagePrints = generatePackagePrints();
//            
//            strategy.matchPrints(packagePrints);
//            Map<Position, Integer> positions = strategy.getPositions();
//            Map<Classification, Integer> classifications = strategy.getClassifications();
//            
//            Date after = new Date();
//            long diff = after.getTime() - before.getTime();
//            
//            new StatsLogger().logStats(positions, classifications, diff);
//            
//       } catch (SQLException | IOException ex) {
//            LOGGER.error(ex.getMessage(), ex);
//        }
        return true;
    }

    private Map<String, PackageHierarchy> generatePackagePrints() throws IOException {
        
        LOGGER.info("* Create Package Prints");
        
        Map<String, String> mappings = new HashMap<>();

        if(options.isObfuscated) {
            
            BufferedReader classReader  = new BufferedReader(new FileReader(options.mappingFile));
            BufferedReader methodReader = new BufferedReader(new FileReader(options.mappingFile));
            
            ProGuardMappingFileParser parser = new ProGuardMappingFileParser(); 
            mappings = parser.parseMappingFileOnMethodLevel(classReader, methodReader);
        } 

        PackageHierarchyGenerator phGen = new PackageHierarchyGenerator(
                options, new ASTToFingerprintTransformer(), mappings);
        
        List<ASTClassBuilder> astClassBuilders = new ArrayList<>(); 
        for(ClassDef classDef: classDefs) {
            ASTClassBuilder astClassBuilder = new ASTClassBuilder(classDef, astBuilderFactory);
            astClassBuilders.add(astClassBuilder);
        }
        
        return phGen.generatePackageHierarchiesFromClassBuilders(astClassBuilders);
        
    }

    private MatchingStrategy setupStrategy() throws SQLException {
        
        EntityService service = EntityServiceFactory.createService();
        FingerprintService fingerprintService = new FingerprintService(service);
        ResultEvaluator evaluator = new ResultEvaluator();
        
        new SetupLogger(options, service).logSetup();
        
        if(options.strategy.equals(InclusionStrategy.class)) {
            FingerprintMatcher matcher = new FingerprintMatcher();
            
            ClassInclusionCalculator classInclusionCalculator = new ClassInclusionCalculator(
                                    matcher, 
                                    options.allowRepeatedMatching
                            );
            
            classInclusionCalculator.setInterestedPackageNames(interestedPackageNames);
            
            PackageInclusionCalculator packageInclusionCalculator = 
                    new PackageInclusionCalculator(
                            classInclusionCalculator,
                            options.allowRepeatedMatching
                    );

            InclusionStrategy strategy = new InclusionStrategy(
                fingerprintService, packageInclusionCalculator, 
                    evaluator, options.inclusionSettings);
            return strategy;
            
        } else if(options.strategy.equals(QuickInclusionCalculator.class)) {
            InclusionStrategy strategy = new InclusionStrategy(
                fingerprintService, new QuickInclusionCalculator(), 
                    evaluator, options.inclusionSettings);
            return strategy;
            
        } else if(options.strategy.equals(DebugObfuscationInvarianceStrategy.class)) {
            return new DebugObfuscationInvarianceStrategy(fingerprintService);

        } else {
            FingerprintMatcher matcher = new FingerprintMatcher(options.similarityThreshold);
            return new VectorDifferenceStrategy(fingerprintService, evaluator, matcher, options.vectorDiffLevel);
        }
    }
}
