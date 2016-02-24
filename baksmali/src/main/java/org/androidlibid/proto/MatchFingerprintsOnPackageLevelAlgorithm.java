package org.androidlibid.proto;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.Package;
import org.androidlibid.proto.ao.Class;
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
    private final ASTToFingerprintTransformer ast2fpt;
    NumberFormat frmt = new DecimalFormat("#0.00");  
    
    private double totalDiffToFirstMatch = 0.0d;
    
    public MatchFingerprintsOnPackageLevelAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options = options;
        this.classDefs = classDefs;
        this.mappings = new HashMap<>();
        this.ast2fpt = new ASTToFingerprintTransformer();
    }
    @Override
    public boolean run() {
        try {
            service = EntityServiceFactory.createService();
            
            if(options.isObfuscated) {
                ProGuardMappingFileParser parser = new ProGuardMappingFileParser(); 
                mappings = parser.parseMappingFile(options.mappingFile);
            }
            
            Map<String, Fingerprint> packagePrints = new HashMap<>();
            
            for(ClassDef def : classDefs) {
                String obfClassName = transformClassName(def.getType());
                String className =    translateName(obfClassName);
                String packageName =  extractPackageName(className);
                
                Fingerprint classFingerprint = transformClassDefToFingerprint(def);
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
            
            FingerprintMatcher matcher = new FingerprintMatcher(1000);
            List<VectorEntity> haystack = new ArrayList<VectorEntity>(service.getPackages());
            int countTotal = 0;
            
            Map<FingerprintMatchTaskResult, Integer> stats = new HashMap<>();
            for(FingerprintMatchTaskResult key : FingerprintMatchTaskResult.values()) {
                stats.put(key, 0);
            }
            
            for(Fingerprint needle : packagePrints.values()) {
                
                //TODO: is this neccessary? are there 3party libst that start with android?
                if(needle.getName().startsWith("android")) continue;
                if(needle.getName().equals("")) continue;
                
                FingerprintMatcher.Result matches = matcher.matchFingerprints(haystack, needle);
                FingerprintMatchTaskResult result = evaluateResult(needle, matches);
                stats.put(result, stats.get(result) + 1);
                countTotal++;
            }
            
            System.out.println("Stats: ");
            System.out.println("Total: " + countTotal);

            for(FingerprintMatchTaskResult key : FingerprintMatchTaskResult.values()) {
                System.out.println(key.toString() + ": " + stats.get(key));
            }
            
            int amountFirstMatches = stats.get(FingerprintMatchTaskResult.OK);
            
            if(amountFirstMatches > 0) {
                System.out.println("avg diff on first machted: " +  frmt.format(totalDiffToFirstMatch / amountFirstMatches));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MatchFingerprintsOnPackageLevelAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MatchFingerprintsOnPackageLevelAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private Fingerprint transformClassDefToFingerprint(ClassDef classDef) throws IOException {
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        List<Node> ast = classDefinition.createAST();
        
        Fingerprint classFingerprint = new Fingerprint();
                
        for(Node node : ast) {
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            classFingerprint.add(methodFingerprint);
        }
        
        return classFingerprint;
    }

    private FingerprintMatchTaskResult evaluateResult(Fingerprint needle, 
            FingerprintMatcher.Result result) throws SQLException {
        
        String packageName = needle.getName();
        Fingerprint nameMatch = result.getMatchByName();
        List<Fingerprint> matchesByDistance = result.getMatchesByDistance();
        
        if(nameMatch == null) {
            System.out.println(packageName + ": not mached by name");
            return FingerprintMatchTaskResult.NO_MATCH_BY_NAME;
        } else {
            
            int i;
            
            for (i = 0; i < matchesByDistance.size(); i++) {
                if(matchesByDistance.get(i).getName().equals(packageName)) {
                    break;
                }
            }
            
            if(i > 0) {
                System.out.println("--------------------------------------------");
                System.out.println("Needle: ");
                System.out.println(needle);
                
                System.out.println("Needle Classes: ");
                for(Fingerprint clazz : needle.getChildren()) {
                    System.out.println("    " + clazz.getName());
                }
                System.out.println("");
                
                System.out.println("Match By Name: ");
                System.out.println(nameMatch);
                
                System.out.println("Match By Name Children:");
                Package nameMatchPackage = (Package) nameMatch.getEntity();
                if(nameMatchPackage != null) {
                    for(Class clazz : nameMatchPackage.getClasses()) {
                        System.out.println("    " + clazz.getName());
                    }
                } else {
                        System.out.println("    [...was null]");
                }
                
                System.out.println("diff: " + frmt.format(needle.euclideanDiff(nameMatch)));
                
                System.out.println("closer matches:");
                for(int j = 0; j < i; j++) {
                    System.out.println(matchesByDistance.get(j).getName() + " ("
                            + frmt.format(needle.euclideanDiff(matchesByDistance.get(j))) + ")");
                }
                
                if(i == matchesByDistance.size()) {
                    System.out.println(packageName + ": not mached by distance.");
                    System.out.println("--------------------------------------------");
                    return FingerprintMatchTaskResult.NO_MATCH_BY_DISTANCE;
                } else {
                    System.out.println(packageName + ": found at position " + (i + 1));
                    System.out.println("--------------------------------------------");
                    return FingerprintMatchTaskResult.NOT_PERFECT;
                } 
            } else {
                double diff = needle.euclideanDiff(nameMatch);
                totalDiffToFirstMatch += diff;
                System.out.println(packageName + ": machted correctly with diff: " + frmt.format(diff) );
                System.out.print("    Diff to next in lines: " );
                
                int counter = 0;
                for (Fingerprint matchByDistance : matchesByDistance) {
                    System.out.print(frmt.format(needle.euclideanDiff(matchByDistance)) + ", ");
                    if(counter++ > 10) break;
                } 
                System.out.print("\n");
                
                return FingerprintMatchTaskResult.OK;
            }
        }
    }

    private String translateName(String obfuscatedName) {
        if(options.isObfuscated && mappings.get(obfuscatedName) != null) {
            return mappings.get(obfuscatedName);
        }
        return obfuscatedName;
    }
    
    public String extractPackageName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public String transformClassName(String className) {
        className = className.replace('/', '.');
        return className.substring(1, className.length() - 1);
    }
}
