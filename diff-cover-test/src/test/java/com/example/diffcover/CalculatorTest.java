package com.example.diffcover;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Calculator sınıfı için testler
 * Kasıtlı olarak tüm metodları test etmiyoruz
 */
public class CalculatorTest {
    
    private Calculator calculator = new Calculator();
    
    @Test
    public void testAdd() {
        assertEquals(8, calculator.add(5, 3));
        assertEquals(0, calculator.add(-5, 5));
        assertEquals(-8, calculator.add(-3, -5));
    }
    
    @Test
    public void testSubtract() {
        assertEquals(2, calculator.subtract(5, 3));
        assertEquals(-10, calculator.subtract(-5, 5));
        assertEquals(2, calculator.subtract(-3, -5));
    }
    
    // multiply, divide, isEven, power, factorial metodları test edilmiyor
    // Bu durum coverage'ın %50'nin altında olmasına neden olacak
}