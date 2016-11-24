package astli.extraction;

import java.util.List;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StringUtils {

    /**
     * Replaces the last occurrence of a string with another string
     * 
     * @see <a href="https://stackoverflow.com/questions/16665387/replace-last-occurrence-of-a-character-in-a-string">Stack Overflow: Replace Last Occurrence of a character in a string</a>
     * 
     * @param string
     * @param substring
     * @param replacement
     * @return String, with last occurrence replaced.
     */
    public static String replaceLastOccurrence(String string, String substring,
            String replacement) {
        
        int index = string.lastIndexOf(substring);
        if(index >= 0) {
            return string.substring(0, index) + replacement + string.substring(index + substring.length());
        } else {
            return string;
        }
    }
    
    public static String implode(double[] list, String glue) {
        
        String[] stringList = new String[list.length];
        
        for(int i = 0; i < list.length; i++) {
            stringList[i] = String.valueOf(list[i]);
            
        }
        
        return implode(stringList, glue);
    }
    
    public static String implode(Object[] list, String glue) {
        StringBuilder builder = new StringBuilder();
        
        for(int i = 0; i < list.length - 1; i++) {
           builder.append(list[i].toString()).append(glue);
        }
        
        if(list.length > 0) {
            builder.append(list[list.length - 1]);
        }
        
        return builder.toString();
    }
    
    public static String implode(List<? extends Object> list, String glue) {
        return implode(list.toArray(), glue);
    }
    
}
