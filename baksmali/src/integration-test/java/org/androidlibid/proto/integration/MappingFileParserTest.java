package org.androidlibid.proto.integration;

import java.io.IOException;
import java.util.HashMap;
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
        
        assert(mapping.get("a.a.b.k.m") != null);
        assert(mapping.get("a.a.b.k.m").equals("org.spongycastle.crypto.macs.SipHash"));
        assert(mapping.size() > 0);
        assert(mapping.get("a.a.b.k") != null);
        assert(mapping.get("a.a.b.k").equals("org.spongycastle.crypto.macs"));
    }
}
