package org.androidlibid.proto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ProGuardMappingFileParser {

    public Map<String, String> parseMappingFile(String mappingFilePath) throws IOException {
        
        Map<String, String> mapping = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(mappingFilePath))) {
            String line;
            
            while ((line = br.readLine()) != null) {
                
                if(line.startsWith("    ")) continue;
                
                String[] pieces = line.split(" -> ");
                
                if(pieces.length != 2) {
                    throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
                }
                
                String obfuscatedClassName = pieces[1].substring(0, pieces[1].length() - 1);
                String clearClassName = pieces[0];
                
                if(obfuscatedClassName.length() == 0 || clearClassName.length() == 0) {
                    throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
                }
                
                mapping.put(obfuscatedClassName, clearClassName);
                
                String obfuscatedPackageName = obfuscatedClassName.substring(0, obfuscatedClassName.lastIndexOf("."));
                String clearPackageName = clearClassName.substring(0, clearClassName.lastIndexOf("."));
                
                mapping.put(obfuscatedPackageName, clearPackageName);
            }
        }
        
        return mapping;
    }
    
}
