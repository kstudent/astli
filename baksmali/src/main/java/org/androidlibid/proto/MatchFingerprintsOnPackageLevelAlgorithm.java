package org.androidlibid.proto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ast.ASTClassDefinition;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchFingerprintsOnPackageLevelAlgorithm implements AndroidLibIDAlgorithm {

    private final List<? extends ClassDef> classDefs;
    private final baksmaliOptions options;
    private EntityService service; 
    private Map<String, String> mappings;
    
    public MatchFingerprintsOnPackageLevelAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options = options;
        this.classDefs = classDefs;
        mappings = new HashMap<>();
    }
    @Override
    public boolean run() {
        try {
            service = EntityServiceFactory.createService();
            
            if(options.isObfuscated) {
                ProGuardMappingFileParser parser = new ProGuardMappingFileParser(); 
                mappings = parser.parseMappingFile(options.mappingFile);
            }
            
            String packageOld = "";
            List<Fingerprint> packagePrints = new LinkedList<>(); 
            Fingerprint currentPackageFingerprint = new Fingerprint();
            
            for(ClassDef def : classDefs) {
                String packageNew = translateName(extractPackageName(def.getType()));
                
                if(!packageNew.equals(packageOld)) {
                    packagePrints.add(currentPackageFingerprint);
                    currentPackageFingerprint = new Fingerprint(packageNew);
                    packageOld = packageNew;
                }
                
                Fingerprint classFingerprint = transformClassDefToFingerprint(def);
                currentPackageFingerprint.add(classFingerprint);
            }
            
            FingerprintMatcher matcher = new FingerprintMatcher(1000);
            List<VectorEntity> haystack = new ArrayList<VectorEntity>(service.getPackages());
            int countTotal = 0;
            
            Map<FingerPrintMatchTaskResult, Integer> stats = new HashMap<>();
            for(FingerPrintMatchTaskResult key : FingerPrintMatchTaskResult.values()) {
                stats.put(key, 0);
            }
            
            for(Fingerprint needle : packagePrints) {
                
                //TODO: is this neccessary? are there 3party libst that start with android?
                if(needle.getName().startsWith("android")) continue;
                
                FingerprintMatcher.Result matches = matcher.matchFingerprints(haystack, needle);
                FingerPrintMatchTaskResult result = evaluateResult(needle, matches);
                stats.put(result, stats.get(result) + 1);
                countTotal++;
            }
            
            System.out.println("Stats: ");
            System.out.println("Total: " + countTotal);

            for(FingerPrintMatchTaskResult key : FingerPrintMatchTaskResult.values()) {
                System.out.println(key.toString() + ": " + stats.get(key));
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(MatchFingerprintsOnPackageLevelAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MatchFingerprintsOnPackageLevelAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private String extractPackageName(String className) {
        className = className.substring(1, className.lastIndexOf("/"));
        return className.replace('/', '.');
    }

    private Fingerprint transformClassDefToFingerprint(ClassDef classDef) throws IOException {
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        List<Node> ast = classDefinition.createAST();
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        String className = translateName(classDef.getType());

        Fingerprint classFingerprint = new Fingerprint(className);
                
        for(Node node : ast) {
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            classFingerprint.add(methodFingerprint);
        }
        
        return classFingerprint;
        
    }

    private FingerPrintMatchTaskResult evaluateResult(Fingerprint needle, 
            FingerprintMatcher.Result result) {
        
        String packageName = needle.getName();
        Fingerprint nameMatch = result.getMatchByName();
        List<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        
        if(nameMatch == null) {
            System.out.println(packageName + ": not mached by name");
            return FingerPrintMatchTaskResult.NO_MATCH_BY_NAME;
        } else {
            
            int i;
            
            for (i = 0; i < matchesByDistance.size(); i++) {
                if(matchesByDistance.get(i).getName().equals(packageName)) {
                    break;
                }
            }
            
            if(i > 0) {
                System.out.println("Needle: ");
                System.out.println(needle);
                System.out.println("Match By Name: ");
                System.out.println(nameMatch);
                System.out.println("diff: " + needle.euclideanDiff(nameMatch));
                
                System.out.println("closer matches:");
                for(int j = 0; j < i; j++) {
                    System.out.println(matchesByDistance.get(j).getName());
                }
                
                if(i == matchesByDistance.size()) {
                    System.out.println(packageName + ": not mached by distance.");
                    return FingerPrintMatchTaskResult.NO_MATCH_BY_DISTANCE;
                } else {
                    System.out.println(packageName + ": found at position " + (i + 1));
                    return FingerPrintMatchTaskResult.NOT_PERFECT;
                } 
            } else {
                return FingerPrintMatchTaskResult.OK;
            }
        }
    }

    private String translateName(String obfuscatedName) {
        if(options.isObfuscated && mappings.get(obfuscatedName) != null) {
            return mappings.get(obfuscatedName);
        }
        return obfuscatedName;
    }
}
