package org.androidlibid.proto.integration;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.androidlibid.proto.match.ProGuardMappingFileParser;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MappingFileParserTest {
    
    @Test
    public void testMappingFileParserClassLevel() throws IOException {
        String mappingFile = "./src/integration-test/resources/MappingFiles/mapping.txt";
        
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser(mappingFile);
        Map<String, String> mapping = parser.parseMappingFileOnClassLevel();
        
        assert(mapping.get("a.a.a.a.a.a") != null);
        assert(mapping.get("a.a.a.a.a.a").equals("org.spongycastle.jcajce.provider.config.ConfigurableProvider"));
        assert(mapping.size() > 0);
        assert(mapping.get("a.a.a.a.a") != null);
        assert(mapping.get("a.a.a.a.a").equals("org.spongycastle.jcajce.provider.config"));
    }
    
    @Test
    public void testMappingFileParserMethodLevel() throws IOException {
        String mappingFile = "./src/integration-test/resources/MappingFiles/mapping.obflvl1.txt";
        
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser(mappingFile);
            Map<String, String> mapping = parser.parseMappingFileOnMethodLevel();
        
        String obfuscatedMethod = "a.a.f.a.ah:b(org.spongycastle.math.ec.SimpleBigDecimal)";
        String clearMethod = "org.spongycastle.math.ec.SimpleBigDecimal:subtract(org.spongycastle.math.ec.SimpleBigDecimal)"; 
        
        assert(mapping.size() > 0);
        assert(mapping.containsKey(obfuscatedMethod));
        assert(mapping.get(obfuscatedMethod).equals(clearMethod));
    }
    
}
