package com.example.diffcover;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StringUtils sınıfı için testler
 */
public class StringUtilsTest {
    
    private StringUtils stringUtils = new StringUtils();
    
    @Test
    public void testIsEmpty() {
        assertTrue(stringUtils.isEmpty(null));
        assertTrue(stringUtils.isEmpty(""));
        assertTrue(stringUtils.isEmpty("   "));
        assertFalse(stringUtils.isEmpty("hello"));
        assertFalse(stringUtils.isEmpty(" hello "));
    }
    
    // reverse, capitalize, countWords metodları test edilmiyor
}