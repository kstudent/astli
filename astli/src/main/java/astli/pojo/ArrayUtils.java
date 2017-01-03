package astli.pojo;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ArrayUtils {
    
    public static short[] truncateIntToShortArray(int[] values) {
        short[] shortVals = new short[values.length];
        for(int i = 0; i < values.length; i++) {
            shortVals[i] = (short) values[i];
        }
        return shortVals;
    }
    
    public static byte[] short2LEByte(short[] values){
    
        byte[] array = new byte[values.length * 2];
        
        for(int i = 0; i < values.length; i++) {
            array[2 * i]     = (byte)( values[i]       & 0xff);
            array[2 * i + 1] = (byte)((values[i] >> 8) & 0xff);
        }
        
        return array;
    }
    
    public static short[] lEByte2Short(byte[] littleEndianBytes){
        
        if (littleEndianBytes.length % 2 == 1) {
            throw new IllegalArgumentException("Byte Array has odd amount of entries.");
        }
        
        short[] array = new short[littleEndianBytes.length / 2];
        
        for(int i = 0; i < littleEndianBytes.length / 2; i++) {
            short left  = (short) ((littleEndianBytes[2 * i + 1] << 8) & 0xff00); 
            short right = (short) (littleEndianBytes[2 * i]            & 0x00ff);
            array[i] = (short) (left | right);
        }
        
        return array;
    }
    
    
    /**
     * [0x____0a0b, 0x____0c0d] -> [0x0b, 0x0a, 0xd, 0xc]
     */
    
    public static byte[] truncateIntToLEByteArray(int[] values) {
        byte[] array = new byte[values.length * 2];
        
        for(int i = 0; i < values.length; i++) {
            array[2 * i]     = (byte)( values[i]       & 0xff);
            array[2 * i + 1] = (byte)((values[i] >> 8) & 0xff);
        }
        
        return array;
    }
}
