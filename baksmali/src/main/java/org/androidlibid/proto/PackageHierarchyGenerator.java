package org.androidlibid.proto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.apache.commons.lang.StringUtils;
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
    
    private static List<String> BLACKLISTED_PACKAGES; 
    private static List<String> WHITELISTED_PACKAGES; 
    
    static {
        BLACKLISTED_PACKAGES = new ArrayList<>();
        BLACKLISTED_PACKAGES.add("android.support");
        WHITELISTED_PACKAGES = new ArrayList<>();
    }
    
    private static final Logger LOGGER = LogManager.getLogger(PackageHierarchyGenerator.class);

    public PackageHierarchyGenerator(baksmaliOptions options, 
            ASTToFingerprintTransformer ast2fpt, Map<String, String> mappings) {
        this.options = options;
        this.ast2fpt = ast2fpt;
        this.mappings = mappings;
    }
    
    public Map<String, PackageHierarchy> generatePackageHierarchiesFromClassBuilders(
            Collection<ASTClassBuilder> astClassBuilders) throws IOException {
        
        Map<String, PackageHierarchy> hierarchies = new HashMap<>();
        
        for(ASTClassBuilder astClassBuilder : astClassBuilders) {
            String smaliClassName = astClassBuilder.getClassName();
            String obfClassName = SmaliNameConverter.convertTypeFromSmali(smaliClassName);
            String className    = translateName(obfClassName);
            String packageName  = SmaliNameConverter.extractPackageNameFromClassName(className);
            
            if(isBlacklisted(packageName)) continue;
            
            PackageHierarchy hierarchy; 
            
            if(hierarchies.containsKey(packageName)) {
                hierarchy = hierarchies.get(packageName);
            } else {
                hierarchy = new PackageHierarchy(packageName);
                hierarchies.put(packageName, hierarchy);
            }
            
            Map<String, Fingerprint> prints = createFingerprintsFromASTBuilder(
                    astClassBuilder, obfClassName);
            
            if(!prints.isEmpty()) {
                hierarchy.addMethods(className, prints);
            }
        }
        
        return hierarchies;
    }
    
    private Map<String, Fingerprint> createFingerprintsFromASTBuilder(
            ASTClassBuilder astClassBuilder, String obfsClassName) throws IOException {
        
        Map<String, Fingerprint> prints = new HashMap<>();
        
        Map<String, Node> methodASTs = astClassBuilder.buildASTs();
        
        for(String obfsMethodSignature : methodASTs.keySet()) {
            Node methodAST = methodASTs.get(obfsMethodSignature);
            Fingerprint print = ast2fpt.createFingerprint(methodAST);
            String clearSignature = getClearSignature(obfsClassName, obfsMethodSignature);
            
            //@TODO Filter out small methods? 
            print.setName(clearSignature);
            prints.put(clearSignature, print);
        }
        
        return prints;
        
    }
    
    private String translateName(String obfuscatedName) {
        if(options.isObfuscated && mappings.get(obfuscatedName) != null) {
            return mappings.get(obfuscatedName);
        }
        return obfuscatedName;
    }

    private String getClearSignature(String obfsClassName, String obfsMethodSignature) {
        
        String obfsIdentifier = obfsClassName + ":" + obfsMethodSignature;
        String clearIdentifier = translateName(obfsIdentifier);
        int identifierInit = StringUtils.ordinalIndexOf(clearIdentifier, ":", 2);
        String clearSignature = clearIdentifier.substring(identifierInit);
        
        return clearSignature;
    }
    
    private boolean isBlacklisted(String packageName) {
        
        if(WHITELISTED_PACKAGES.isEmpty()) {
            return BLACKLISTED_PACKAGES.stream()
                    .anyMatch(pckg -> packageName.startsWith(pckg));
        } else {
            return !WHITELISTED_PACKAGES.stream()
                    .anyMatch(pckg -> packageName.startsWith(pckg));
        }
        
    }
}