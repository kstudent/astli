package org.androidlibid.proto.match;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.PackageHierarchyGenerator;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
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
       
    public MatchFingerprintsAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options   = options;
        this.classDefs = classDefs;
    }
    
    @Override
    public boolean run() {
        try {
            EntityService service = EntityServiceFactory.createService();
            
            FingerprintService fingerprintService = new FingerprintService(service);
            
            PackageInclusionCalculator packageInclusionCalculator = 
                    new PackageInclusionCalculator(
                            new ClassInclusionCalculator(
                                    new FingerprintMatcher(1000)));
            
            MatchingStrategy strategy = new MatchOnMethodLevelWithInclusionStrategy(
                fingerprintService, packageInclusionCalculator, 
                    new ResultEvaluator(fingerprintService));
            
            Map<String, Fingerprint> packagePrints = generatePackagePrints();
            
            Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(packagePrints);
            
            LOGGER.info("Stats: ");
            for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
                LOGGER.info("{}: {}", new Object[]{key.toString(), stats.get(key)});
            }
            
       } catch (SQLException | IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return true;
    }

    private Map<String, Fingerprint> generatePackagePrints() throws IOException {
        
        Map<String, String> mappings = new HashMap<>();

        if(options.isObfuscated) {
            ProGuardMappingFileParser parser = new ProGuardMappingFileParser(options.mappingFile); 
            mappings = parser.parseMappingFileOnMethodLevel();
        } 

        PackageHierarchyGenerator phGen = new PackageHierarchyGenerator(
                options, new ASTToFingerprintTransformer(), mappings);
        
        return phGen.generatePackageHierarchyFromClassDefs(classDefs);
        
    }
}
