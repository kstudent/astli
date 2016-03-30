package org.androidlibid.proto.utils;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StringUtils {

    /**
     * Replaces the last occurrence of a string with another string
     * 
     * @source: https://stackoverflow.com/questions/16665387/replace-last-occurrence-of-a-character-in-a-string
     * 
     * @param string
     * @param substring
     * @param replacement
     * @return 
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
    
}
