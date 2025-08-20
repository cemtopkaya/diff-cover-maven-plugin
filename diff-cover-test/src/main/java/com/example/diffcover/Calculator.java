package com.example.diffcover;

/**
 * Basit hesap makinesi sınıfı
 * Bu sınıfta kasıtlı olarak test edilmeyen metodlar var
 */
public class Calculator {
    
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    // Bu metod test edilmeyecek
    public int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return a * b;
    }
    
    // Bu metod da test edilmeyecek
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Sıfıra bölme hatası");
        }
        return a / b;
    }
    
    // Bu metod hiç test edilmeyecek
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
    
    // Bu metod da test edilmeyecek
    public int power(int base, int exponent) {
        if (exponent == 0) {
            return 1;
        }
        int result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }
    
    // Bu metod test edilmeyecek
    public int factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negatif sayının faktöriyeli hesaplanamaz");
        }
        if (n == 0 || n == 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }
}