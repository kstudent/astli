package org.androidlibid.proto.ast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.androidlibid.proto.pojo.PackageHierarchy;
import org.androidlibid.proto.pojo.ASTLIOptions;

import java.io.IOException;
import java.util.List;
import org.androidlibid.proto.utils.ProGuardMappingFileParser;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyStreamGenerator {

    private final ASTLIOptions astliOptions;
    private final List<? extends ClassDef> classDefs;
    private final ASTBuilderFactory astBuilderFactory;

    public PackageHierarchyStreamGenerator(ASTLIOptions astliOptions, List<? extends ClassDef> classDefs, ASTBuilderFactory astBuilderFactory) {
        this.astliOptions = astliOptions;
        this.classDefs = classDefs;
        this.astBuilderFactory = astBuilderFactory;
    }
    
    public Stream<PackageHierarchy> generateStream() throws IOException {
        
        Map<String, String> mappings = new HashMap<>();

        if(astliOptions.isObfuscated()) {
            
            BufferedReader classReader  = new BufferedReader(new FileReader(astliOptions.mappingFile));
            BufferedReader methodReader = new BufferedReader(new FileReader(astliOptions.mappingFile));
            
            ProGuardMappingFileParser parser = new ProGuardMappingFileParser(); 
            mappings = parser.parseMappingFileOnMethodLevel(classReader, methodReader);
        } 

        PackageHierarchyGenerator phGen = new PackageHierarchyGenerator(
                new ASTToFingerprintTransformer(), mappings);
        
        Stream<ASTClassBuilder> builderStream = classDefs.parallelStream()
                .map(classDef -> new ASTClassBuilder(classDef, astBuilderFactory));
        
        return phGen.generatePackageHierarchiesFromClassBuilders(builderStream);
        
    }

    
}
