package org.androidlibid.proto.integration;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.androidlibid.proto.ProGuardMappingFileParser;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MappingFileParserTest {
    
    @Test
    public void testMappingFileParser() throws IOException {
        String mappingFile = "./src/integration-test/resources/MappingFiles/mapping.txt";
        
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser();
        Map<String, String> mapping = parser.parseMappingFile(mappingFile);
        
        assert(mapping.get("a.a.a.a.a.a") != null);
        assert(mapping.get("a.a.a.a.a.a").equals("org.spongycastle.jcajce.provider.config.ConfigurableProvider"));
        assert(mapping.size() > 0);
        assert(mapping.get("a.a.a.a.a") != null);
        assert(mapping.get("a.a.a.a.a").equals("org.spongycastle.jcajce.provider.config"));
    }
}
