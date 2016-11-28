package astli.extraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import astli.pojo.ASTLIOptions;

import java.io.IOException;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MappingHandler {

    private final ASTLIOptions astliOptions;

    public MappingHandler(ASTLIOptions astliOptions) {
        this.astliOptions = astliOptions;
    }
    
    public Map<String, String> getMapping() throws IOException {
        
        Map<String, String> mappings = new HashMap<>();

        if(astliOptions.isObfuscated()) {
            
            BufferedReader classReader  = new BufferedReader(new FileReader(astliOptions.mappingFile));
            BufferedReader methodReader = new BufferedReader(new FileReader(astliOptions.mappingFile));
            
            ProGuardMappingFileParser parser = new ProGuardMappingFileParser(); 
            mappings = parser.parseMappingFileOnMethodLevel(classReader, methodReader);
        } 
        
        return mappings;
    }

    
}
