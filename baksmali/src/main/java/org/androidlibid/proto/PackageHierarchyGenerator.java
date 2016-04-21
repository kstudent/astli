package org.androidlibid.proto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.baksmaliOptions;

/**
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyGenerator {

    private final baksmaliOptions options;
    private final ASTToFingerprintTransformer ast2fpt;
    private final Map<String, String> mappings;
    
    private static final Logger LOGGER = LogManager.getLogger(PackageHierarchyGenerator.class);
    private Set<String> interestedPackageNames = new HashSet<>();

    public PackageHierarchyGenerator(baksmaliOptions options, ASTToFingerprintTransformer ast2fpt, Map<String, String> mappings) {
        this.options = options;
        this.ast2fpt = ast2fpt;
        this.mappings = mappings;
    }
    
    public Map<String, Fingerprint> generatePackageHierarchyFromClassBuilders(List<ASTClassBuilder> astClassBuilders) throws IOException {
        
        Map<String, Fingerprint> packagePrints = new HashMap<>();
            
        for(ASTClassBuilder astClassBuilder : astClassBuilders) {
            String smaliClassName = astClassBuilder.getClassName();
            
            String obfClassName = SmaliNameConverter.convertTypeFromSmali(smaliClassName);
            String className =    translateName(obfClassName);
            String packageName =  SmaliNameConverter.extractPackageNameFromClassName(className);
            
            if(!interestedPackageNames.isEmpty() && !interestedPackageNames.contains(packageName)) {
                continue;
            }
            
            Fingerprint classFingerprint = transformClassDefToFingerprint(astClassBuilder, obfClassName);
            
            if(classFingerprint.getChildFingerprints().isEmpty()) {
                continue;
            }
            
            classFingerprint.setName(className);
            Fingerprint packageFingerprint;

            if(packagePrints.containsKey(packageName)) {
                packageFingerprint = packagePrints.get(packageName);
            } else {
                packageFingerprint = new Fingerprint(packageName);
                packagePrints.put(packageName, packageFingerprint);
            }

            packageFingerprint.sumFeatures(classFingerprint);
            packageFingerprint.addChildFingerprint(classFingerprint);
        }
        
        for(Fingerprint pckg : packagePrints.values()) {
            Collections.sort(pckg.getChildFingerprints(), Fingerprint.sortByLengthDESC);
        }

        return packagePrints;
    }
    
    private Fingerprint transformClassDefToFingerprint(ASTClassBuilder astClassBuilder, String obfsClassName) throws IOException {
        
        Map<String, Node> ast = astClassBuilder.buildASTs();
        
        List<Fingerprint> methods = new ArrayList<>(); 
        Fingerprint classFingerprint = new Fingerprint();

        for(String obfsMethodSignature : ast.keySet()) {
            Node node = ast.get(obfsMethodSignature);
            
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            
            String obfsIdentifier = obfsClassName + ":" + obfsMethodSignature; 
            
            String clearMethodSignature = translateName(obfsIdentifier);
            LOGGER.info("* {}, (obs: {} )", clearMethodSignature, obfsIdentifier);
            LOGGER.info("** ast" );
            LOGGER.info(ast.get(obfsMethodSignature));
            LOGGER.info("** fingerprint" );
            LOGGER.info(methodFingerprint);
            
            if(methodFingerprint.getLength() > 1.0f) {
                methodFingerprint.setName(clearMethodSignature);
                methods.add(methodFingerprint);
                classFingerprint.sumFeatures(methodFingerprint);
            }
        }
        
        Collections.sort(methods, Fingerprint.sortByLengthDESC);
        
        for(Fingerprint method : methods) {
            classFingerprint.addChildFingerprint(method);
        }
        
        return classFingerprint;
    }

    private String translateName(String obfuscatedName) {
        if(options.isObfuscated && mappings.get(obfuscatedName) != null) {
            return mappings.get(obfuscatedName);
        }
        return obfuscatedName;
    }

    public void setInterestedPackageNames(Set<String> interestedPackageNames) {
        this.interestedPackageNames = interestedPackageNames;
    }
}