package com.example.diffcover;

/**
 * Ana uygulama sınıfı
 */
public class App {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        StringUtils stringUtils = new StringUtils();
        
        System.out.println("=== Hesap Makinesi Test ===");
        System.out.println("5 + 3 = " + calc.add(5, 3));
        System.out.println("10 - 4 = " + calc.subtract(10, 4));
        
        System.out.println("\n=== String İşlemleri ===");
        System.out.println("'hello' boş mu? " + stringUtils.isEmpty("hello"));
        System.out.println("'' boş mu? " + stringUtils.isEmpty(""));
        
        // Bu metodlar test edilmediği için coverage düşük olacak
        System.out.println("3 * 4 = " + calc.multiply(3, 4));
        System.out.println("'world' tersten: " + stringUtils.reverse("world"));
    }
}