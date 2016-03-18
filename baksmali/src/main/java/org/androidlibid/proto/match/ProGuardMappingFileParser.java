package org.androidlibid.proto.match;

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
    
    Map<String, String> mapping = new HashMap<>();
    String mappingFilePath; 

    public ProGuardMappingFileParser(String mappingFilePath) {
        this.mappingFilePath = mappingFilePath;
    }

    public Map<String, String> parseMappingFileOnClassLevel() throws IOException {
        
        try (BufferedReader br = new BufferedReader(new FileReader(mappingFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("    ")) continue;
                parseClassLine(line);
            }
        }
        
        return mapping;
    }
    
    public Map<String, String> parseMappingFileOnMethodLevel() throws IOException {
        
        try (BufferedReader br = new BufferedReader(new FileReader(mappingFilePath))) {
            String line;
            
            String[] classNames = {"", ""}; 
            
            while ((line = br.readLine()) != null) {
                
                if(line.startsWith("    ")) {
                    parseMethodLine(line.substring(4), classNames[0], classNames[1]);
                } else {
                    classNames = parseClassLine(line);
                    if(classNames[0].equals("a.a.c.a")) {
                        System.out.println("Good time to start debugging");
                    }
                    if(classNames[1].equals("a.a.c.a")) {
                        System.out.println("Also a Good time to start debugging");
                    }
                }
            }
        }
        
        return mapping;
    }

    private String[] parseClassLine(String line) {
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
        
        String[] classNames = {obfuscatedClassName, clearClassName};
        
        return classNames;
    }
    
    private void parseMethodLine(String line, String obfuscatedClassName, String clearClassName) {
        String[] pieces = line.split(" -> ");
        if(pieces.length != 2) {
            throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
        }
        
        String clearNameString = pieces[0]; 
        int start = clearNameString.indexOf(" ");
        int end = clearNameString.indexOf("(", start);
        
        if(start < 0 || end < 0) {
            return;
        }
        
        String clearMethodName = clearNameString.substring(start + 1, end);
        
        String obfuscatedMethodName = pieces[1];
        
        if(obfuscatedClassName.length() == 0 || clearClassName.length() == 0 
                || clearMethodName.length() == 0 || obfuscatedMethodName.length() == 0) {
            throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
        }
        
        mapping.put(obfuscatedClassName + ":" + obfuscatedMethodName, clearClassName + ":" + clearMethodName);
    }
    
}
