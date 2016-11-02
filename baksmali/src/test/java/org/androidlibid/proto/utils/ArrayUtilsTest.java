package org.androidlibid.proto.utils;

import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ArrayUtilsTest {
    
    @Test
    public void testShortRoundtrip() {
        short[] shorts = new short[]{0x0000, 0x0A0B, 0x7FFF, 0x0001, 0x0100, 0x00ff, 0x7f00};
        byte[] bytes = ArrayUtils.short2LEByte(shorts);
        short[] shorts2 = ArrayUtils.lEByte2Short(bytes);
        assert(Arrays.equals(shorts, shorts2));
    }
    
    @Test
    public void testByteRoundtrip() {
        byte[] bytes = new byte[]{0x00, 0x00, 0x01, 0x02, 0x7f, 0x7f, (byte)0xff, (byte)0xff};
        short[] shorts = ArrayUtils.lEByte2Short(bytes);
        byte[] bytes2 = ArrayUtils.short2LEByte(shorts);
        assert(Arrays.equals(bytes, bytes2));
    }
    
}
