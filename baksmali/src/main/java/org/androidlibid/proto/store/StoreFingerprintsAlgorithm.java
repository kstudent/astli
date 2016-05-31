package org.androidlibid.proto.store;

import java.io.IOException;
import org.androidlibid.proto.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.androidlibid.proto.PackageHierarchy;
import org.androidlibid.proto.PackageHierarchyGenerator;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.androidlibid.proto.ast.ASTBuilderFactory;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ao.PackageHierarchyService;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreFingerprintsAlgorithm implements AndroidLibIDAlgorithm {

    private final List<? extends ClassDef> classDefs;
    private final baksmaliOptions options;
    
    private static final Logger LOGGER = LogManager.getLogger(StoreFingerprintsAlgorithm.class);
    private EntityService service;

    public StoreFingerprintsAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options   = options;
        this.classDefs = classDefs;
    }
    
    @Override
    public boolean run() {   
        try {
            service = EntityServiceFactory.createService();
            storeFingerprints();
        } catch (SQLException | InterruptedException | ExecutionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return true;
    }
    
    private void storeFingerprints() throws InterruptedException, ExecutionException, SQLException {
        
        final ASTBuilderFactory astBuilderFactory = new ASTBuilderFactory(options);
//        final FingerprintService fpService = new FingerprintService(service);
        final PackageHierarchyService phService = new PackageHierarchyService(service, options.mvnIdentifier);
        final ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        final PackageHierarchyGenerator phgen = new PackageHierarchyGenerator(options, ast2fpt, new HashMap<String, String>());
        
        List<ASTClassBuilder> builders = new ArrayList<>();
        
        for (final ClassDef classDef: classDefs) {
            ASTClassBuilder astClassBuilder = new ASTClassBuilder(classDef, astBuilderFactory);
            builders.add(astClassBuilder);
        }
        
        try {
            Map<String, PackageHierarchy> packages = phgen.generatePackageHierarchiesFromClassBuilders(builders);
            phService.saveHierarchies(packages.values());
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }
}
