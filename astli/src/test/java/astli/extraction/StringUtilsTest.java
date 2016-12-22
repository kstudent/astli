package astli.extraction;

import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StringUtilsTest {
    
    @Test
    public void testImplode(){
        String glued = StringUtils.implode(new double[]{10.0, 1.0}, ",");
        assert("10.0,1.0".equals(glued));
    }
    
}
