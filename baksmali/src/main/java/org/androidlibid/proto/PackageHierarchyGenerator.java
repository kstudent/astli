package org.androidlibid.proto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.ast.ASTClassDefinition;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyGenerator {

    private final baksmaliOptions options;
    private final ASTToFingerprintTransformer ast2fpt;
    private final Map<String, String> mappings;
    
    private static final Logger LOGGER = LogManager.getLogger(PackageHierarchyGenerator.class);

    private final Comparator<Fingerprint> sortByEuclidDESCComparator = new Comparator<Fingerprint>() {
        @Override
        public int compare(Fingerprint that, Fingerprint other) {
            double thatNeedleLength  = that.euclideanNorm();
            double otherNeedleLength = other.euclideanNorm();
            if (thatNeedleLength > otherNeedleLength) return -1;
            if (thatNeedleLength < otherNeedleLength) return  1;
            return 0;
        }
    };
    
    public PackageHierarchyGenerator(baksmaliOptions options, ASTToFingerprintTransformer ast2fpt, Map<String, String> mappings) {
        this.options = options;
        this.ast2fpt = ast2fpt;
        this.mappings = mappings;
    }
    
    public Map<String, Fingerprint> generatePackageHierarchyFromClassDefs(List<ASTClassDefinition> astClassDefinitions) throws IOException {
        
        Map<String, Fingerprint> packagePrints = new HashMap<>();
            
        for(ASTClassDefinition astClassDefinition : astClassDefinitions) {
            ClassDef classDef = astClassDefinition.getClassDef();
            
            String obfClassName = SmaliNameConverter.convertTypeFromSmali(classDef.getType());
            String className =    translateName(obfClassName);
            String packageName =  SmaliNameConverter.extractPackageNameFromClassName(className);
            
            Fingerprint classFingerprint = transformClassDefToFingerprint(astClassDefinition, obfClassName);
            
            if(classFingerprint.getChildren().isEmpty()) {
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

            packageFingerprint.add(classFingerprint);
            packageFingerprint.addChild(classFingerprint);
        }
        
        for(Fingerprint pckg : packagePrints.values()) {
            Collections.sort(pckg.getChildren(), sortByEuclidDESCComparator);
        }

        return packagePrints;
    }
    
    private Fingerprint transformClassDefToFingerprint(ASTClassDefinition classDef, String obfsClassName) throws IOException {
        
        Map<String, Node> ast = classDef.createASTwithNames();
        
        List<Fingerprint> methods = new ArrayList<>(); 
        Fingerprint classFingerprint = new Fingerprint();

        for(String obfsMethodSignature : ast.keySet()) {
            Node node = ast.get(obfsMethodSignature);
            
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            
            String obfsIdentifier = obfsClassName + ":" + obfsMethodSignature; 
            
            String clearMethodSignature = translateName(obfsIdentifier);
            LOGGER.debug("* {}, (obs: {} )", clearMethodSignature, obfsIdentifier);
            LOGGER.debug("** ast" );
            LOGGER.debug(ast.get(obfsMethodSignature));
            LOGGER.debug("** fingerprint" );
            LOGGER.debug(methodFingerprint);
            
            if(methodFingerprint.euclideanNorm() > 1.0f) {
                methodFingerprint.setName(clearMethodSignature);
                methods.add(methodFingerprint);
                classFingerprint.add(methodFingerprint);
            }
        }
        
        Collections.sort(methods, sortByEuclidDESCComparator);
        
        for(Fingerprint method : methods) {
            classFingerprint.addChild(method);
        }
        
        return classFingerprint;
    }

    private String translateName(String obfuscatedName) {
        if(options.isObfuscated && mappings.get(obfuscatedName) != null) {
            return mappings.get(obfuscatedName);
        }
        return obfuscatedName;
    }
}