package org.androidlibid.proto.match;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import org.androidlibid.proto.SmaliNameConverter;
import org.androidlibid.proto.utils.StringUtils;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ProGuardMappingFileParser {
    
    BiMap<String, String> mapping = HashBiMap.create();
    String mappingFilePath; 

    public ProGuardMappingFileParser(String mappingFilePath) {
        this.mappingFilePath = mappingFilePath;
    }

    public Map<String, String> parseMappingFileOnClassLevel() throws IOException {
        
        try (BufferedReader br = new BufferedReader(new FileReader(mappingFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("    ")) continue;
                parseClassLine(line, true);
            }
        }
        
        return mapping;
    }
    
    public Map<String, String> parseMappingFileOnMethodLevel() throws IOException {
        
        parseMappingFileOnClassLevel();
        
        try (BufferedReader br = new BufferedReader(new FileReader(mappingFilePath))) {
            String line;
            
            String[] classNames = {"", ""}; 
            
            while ((line = br.readLine()) != null) {
                
                if(line.startsWith("    ")) {
                    parseMethodLine(line.substring(4), classNames[0], classNames[1]);
                } else {
                    classNames = parseClassLine(line, false);
                }
            }
        }
        
        return mapping;
    }

    private String[] parseClassLine(String line, boolean addToMapping) {
        String[] pieces = line.split(" -> ");

        if(pieces.length != 2) {
            throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
        }

        String obfuscatedClassName = pieces[1].substring(0, pieces[1].length() - 1);
        String clearClassName = pieces[0];

        if(obfuscatedClassName.length() == 0 || clearClassName.length() == 0) {
            throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
        }

        obfuscatedClassName = StringUtils.replaceLastOccurrence(obfuscatedClassName, ".", ":");
        clearClassName      = StringUtils.replaceLastOccurrence(clearClassName,      ".", ":");
        
        String obfuscatedPackageName = SmaliNameConverter.extractPackageNameFromClassName(obfuscatedClassName);
        String clearPackageName      = SmaliNameConverter.extractPackageNameFromClassName(clearClassName);
        
        if(addToMapping) {
            
            if(mapping.containsKey(obfuscatedClassName) || mapping.containsValue(clearClassName)) {
                throw new RuntimeException("Mapping file format error: " + 
                        obfuscatedClassName + " or " + clearClassName + " are already mapped");
            } else {
                mapping.put(obfuscatedClassName, clearClassName);
            }
            
            if(!mapping.containsKey(obfuscatedPackageName) && !mapping.containsValue(clearPackageName)) {
                mapping.put(obfuscatedPackageName, clearPackageName);
            } 
        }
        
        return new String[] {obfuscatedClassName, clearClassName};
    }
    
    private void parseMethodLine(String line, String obfuscatedClassName, String clearClassName) {
        String[] pieces = line.split(" -> ");
        if(pieces.length != 2) {
            throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
        }
        
        String clearNameString = pieces[0]; 
        int clearNameStart = clearNameString.indexOf(" ");
        int argumentStart  = clearNameString.indexOf("(", clearNameStart);
        int argumentEnd    = clearNameString.indexOf(")", argumentStart);
        
        if(clearNameStart < 0 || argumentStart < 0 || argumentEnd < 0) {
            return;
        }
        
        String clearReturnType = clearNameString.substring(0, clearNameStart);
        String clearMethodName = clearNameString.substring(clearNameStart + 1, argumentStart);
        String clearArguments  = clearNameString.substring(argumentStart  + 1, argumentEnd);
        
        String obfuscatedArguments  = obfuscateTypes(clearArguments); 
        String obfuscatedMethodName = pieces[1];
        String obfuscatedReturnType = obfuscateType(clearReturnType); 
        
        clearReturnType = StringUtils.replaceLastOccurrence(clearReturnType, ".", ":");
        clearArguments = replaceLastOccurencesOfArguments(clearArguments);
        
        if(obfuscatedClassName.isEmpty() || clearClassName.isEmpty()
                || clearMethodName.isEmpty() || obfuscatedMethodName.isEmpty()) {
            throw new RuntimeException("Mapping file " + mappingFilePath + ": format error");
        }
       
        String obfuscatedKey  = obfuscatedClassName + ":" + obfuscatedMethodName + "(" + obfuscatedArguments + "):" + obfuscatedReturnType; 
        String clearNameValue = clearClassName      + ":" + clearMethodName      + "(" + clearArguments      + "):" + clearReturnType; 
        
        try {
            mapping.put(obfuscatedKey, clearNameValue);
        } catch (IllegalArgumentException ex) {
            //TODO: Logger
            System.out.println("Duplicate pair: " + obfuscatedKey + "->" + clearNameValue);
        }
    }

    private String obfuscateTypes(String types) {
        
        if(types.isEmpty()) {
            return ""; 
        }
        
        StringBuilder obfuscatedTypes = new StringBuilder();
        
        String[] pieces = types.split(SmaliNameConverter.TYPE_DELIMITER);
        
        for(int i = 0; i < pieces.length; i++) {
            String type = pieces[i];
            String obfuscatedType = obfuscateType(type);
            obfuscatedTypes.append(obfuscatedType);
            
            if(i < pieces.length - 1) {
                obfuscatedTypes.append(SmaliNameConverter.TYPE_DELIMITER);
            }
        }
        
        return obfuscatedTypes.toString();
    
    }
    
    private String obfuscateType(String type) {
        
        if(type.isEmpty()) {
            return ""; 
        }
        
        String[] pieces = type.split("\\[");
        
        String clearType = pieces[0];
        String obfuscatedType;
        
        if(SmaliNameConverter.isPrimitiveType(clearType)) {
            
            obfuscatedType = clearType;
            
        } else {
            
            clearType = StringUtils.replaceLastOccurrence(clearType, ".", ":");
            obfuscatedType = mapping.inverse().get(clearType);

            if(obfuscatedType == null) {
                obfuscatedType = clearType;
            }
        
        }

        if(pieces.length == 1) {

            return obfuscatedType;

        } else {
            
            StringBuilder obfuscatedTypeBuilder = new StringBuilder(obfuscatedType);
            
            for(int i = 0; i < pieces.length - 1; i++) {
                obfuscatedTypeBuilder.append("[]");
            }
            
            return obfuscatedTypeBuilder.toString();
        }
    }

    private String replaceLastOccurencesOfArguments(String oldArguments) {
        
        if(oldArguments.isEmpty()) {
            return ""; 
        }
        
        StringBuilder clearArguments = new StringBuilder();
        
        String[] pieces = oldArguments.split(SmaliNameConverter.TYPE_DELIMITER);
        
        for(int i = 0; i < pieces.length; i++) {
            String oldArgument = pieces[i];
            String clearArgument = StringUtils.replaceLastOccurrence(oldArgument, ".", ":");
            clearArguments.append(clearArgument);
            
            if(i < pieces.length - 1) {
                clearArguments.append(SmaliNameConverter.TYPE_DELIMITER);
            }
        }
        
        return clearArguments.toString();
    }
}