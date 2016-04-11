package org.androidlibid.proto.utils;

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
    
}
