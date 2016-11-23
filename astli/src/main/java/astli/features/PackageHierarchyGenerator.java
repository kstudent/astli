package astli.features;

import astli.utils.SmaliNameConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyGenerator {

    private final ASTToFingerprintTransformer ast2fpt;
    private final Map<String, String> mappings;
    private final boolean isObfuscated;
    
    
    private static final List<String> BLACKLISTED_PACKAGES; 
    private static final List<String> WHITELISTED_PACKAGES; 
    
    static {
        BLACKLISTED_PACKAGES = new ArrayList<>();
        BLACKLISTED_PACKAGES.add("android.support");
        WHITELISTED_PACKAGES = new ArrayList<>();
    }
    
    public PackageHierarchyGenerator(ASTToFingerprintTransformer ast2fpt, 
            Map<String, String> mappings) {
        this.ast2fpt = ast2fpt;
        this.mappings = mappings;
        this.isObfuscated = !(mappings.isEmpty());
        
    }
    
    public Stream<PackageHierarchy> generatePackageHierarchiesFromClassBuilders(
            Stream<ASTClassBuilder> astClassBuilderStream) {
        
        Map<String, PackageHierarchy> hierarchies = new HashMap<>();
        
        astClassBuilderStream
                .map(builder -> createRecordFromASTBuilder(builder))
                .filter(record -> !record.getMethods().isEmpty())
                .forEach(record  -> insertRecordIntoHierarchies(record, hierarchies));
        
        return hierarchies.values().parallelStream();
    }
     
    /**
     * 
     * @throws unchecked IOException
     * @param astClassBuilder
     * @return 
     */
    private Record createRecordFromASTBuilder(ASTClassBuilder astClassBuilder) {
        
        Map<String, Fingerprint> methods = new HashMap<>();
        
        String smaliClassName = astClassBuilder.getClassName();
        String obfClassName   = SmaliNameConverter.convertTypeFromSmali(smaliClassName);
        String className      = translateName(obfClassName);
        String packageName    = SmaliNameConverter.extractPackageNameFromClassName(className);
        
        if(isBlacklisted(packageName)) {
            return new Record(methods, className);
        } else {
            methods = createFingerprintsFromASTBuilder(astClassBuilder);
            return new Record(methods, className);
        } 
    }
    
    private void insertRecordIntoHierarchies(Record record, Map<String, PackageHierarchy> hierarchies) {
            
        synchronized(hierarchies) {
            
            String packageName = record.getPackageName();
            
            PackageHierarchy hierarchy; 
            if(hierarchies.containsKey(packageName)) {
                hierarchy = hierarchies.get(packageName);
            } else {
                hierarchy = new PackageHierarchy(packageName);
                hierarchies.put(packageName, hierarchy);
            }
            
            hierarchy.addMethods(record.className, record.getMethods());
        }
        
    }
       
    /**
     * 
     * @throws unchecked IOException
     * @param astClassBuilder
     * @return 
     */
    private Map<String, Fingerprint> createFingerprintsFromASTBuilder(
            ASTClassBuilder astClassBuilder) {
        
        try
        {
            String obfsClassName = getClassName(astClassBuilder);

            Map<String, Fingerprint> prints = new HashMap<>();

            Map<String, Node> methodASTs = astClassBuilder.buildASTs();

            for(String obfsMethodSignature : methodASTs.keySet()) {
                Node methodAST = methodASTs.get(obfsMethodSignature);
                Fingerprint print = ast2fpt.createFingerprint(methodAST);
                String clearSignature = getClearSignature(obfsClassName, obfsMethodSignature);

                print.setName(clearSignature);
                prints.put(clearSignature, print);
            }

            return prints;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
        
    }
    
    private String getClassName(ASTClassBuilder astClassBuilder) {
        String smaliClassName = astClassBuilder.getClassName();
        return SmaliNameConverter.convertTypeFromSmali(smaliClassName);
    }
    
    private String translateName(String obfuscatedName) {
        if(isObfuscated && mappings.get(obfuscatedName) != null) {
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

    private static class Record {
        private final Map<String, Fingerprint> methods;
        private final String className;

        public Record(Map<String, Fingerprint> methods, String className) {
            this.methods = methods;
            this.className = className;
        }

        public Map<String, Fingerprint> getMethods() {
            return methods;
        }

        public String getClassName() {
            return className;
        }
        
        public String getPackageName() {
            return SmaliNameConverter.extractPackageNameFromClassName(className);
        }
    }
}