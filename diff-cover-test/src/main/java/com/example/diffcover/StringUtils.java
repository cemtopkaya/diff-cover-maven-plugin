package com.example.diffcover;

/**
 * String işlemleri için yardımcı sınıf
 */
public class StringUtils {
    
    public boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    // Bu metod test edilmeyecek
    public String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }
    
    // Bu metod test edilmeyecek
    public String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    // Bu metod test edilmeyecek
    public int countWords(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        String[] words = str.trim().split("\\s+");
        return words.length;
    }
}